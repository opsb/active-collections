package com.googlecode.activecollections;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import com.googlecode.activecollections.ActiveSet;

public class ActiveSetSpec {

	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class EmptyPeopleSpec {

		private final Person PAUL = new Person("Paul");
		private final Person PETER = new Person("Peter");

		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private ActiveSet<Person> people;

		@Before
		public void context() {
			
			people = new People(entityManagerFactory);
		}

		@Test
		public void canAddNewPerson() {

			people.add(PAUL);
			assertThat(people.size(), equalTo(1));

		}

		@Test
		public void canUpdatePerson() {
			people.add(PAUL);
			people.add(PAUL);
			assertThat(people.size(), equalTo(1));
		}

		@Test
		public void sizeShouldBeZero() {
			assertThat(people.size(), equalTo(0));
		}

		@Test
		public void shouldBeEmpty() {
			assertTrue(people.isEmpty());
		}

		@Test
		public void shouldNotContainPaul() {
			assertFalse(people.contains(PAUL));
		}

		@Test
		public void shouldNotContainPaulOrPeter() {
			assertFalse(people.containsAll(asList(PAUL, PETER)));
		}
		
		@Test
		public void canAddAll() {
			List<Person> paulAndPeter = asList( PAUL, PETER );
			people.addAll( paulAndPeter );
			assertTrue( people.containsAll( paulAndPeter) );
		}
		
		@Test
		public void arrayIsEmpty() {
			
			assertTrue(people.toArray().length == 0);
			
		}
		
		@Test
		public void canClearAll() {
			people.clear();
			assertTrue(people.isEmpty());
		}

	}

	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithOnePersonSpec {

		private final Person PAUL = new Person("Paul");
		private final Person PETER = new Person("Peter");

		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private ActiveSet<Person> people;

		@Before
		public void context() {
			people = new People(entityManagerFactory);
			people.add(PETER);
		}

		@Test
		public void canAddNewPerson() {

			people.add(PAUL);
			assertThat(people.size(), equalTo(2));

		}

		@Test
		public void canUpdatePerson() {
			people.add(PAUL);
			people.add(PAUL);
			assertThat(people.size(), equalTo(2));
		}

		@Test
		public void sizeShouldBeOne() {
			assertThat(people.size(), equalTo(1));
		}

		@Test
		public void shouldNotBeEmpty() {
			assertFalse(people.isEmpty());
		}

		@Test
		public void shouldContainPeter() {
			assertTrue(people.contains(PETER));
		}

		@Test
		public void shouldNotContainPaulAndPeter() {
			assertFalse(people.containsAll(asList(PAUL, PETER)));
		}
		
		@Test
		public void canRemovePeter() {
			people.remove(PETER);
			assertFalse(people.contains(PETER));
		}
		
		@Test
		public void canClearAll() {
			people.clear();
			assertTrue(people.isEmpty());
		}

	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithTwoPeopleSpec {

		private final Person PAUL = new Person("Paul");
		private final Person PETER = new Person("Peter");
		private final Person MARK = new Person("Mark");
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private ActiveSet<Person> people;

		@Before
		public void context() {
			people = new People(entityManagerFactory);
			people.add(PETER);
			people.add(PAUL);
		}

		@Test
		public void canAddNewPerson() {

			people.add(MARK);
			assertThat(people.size(), equalTo(3));

		}

		@Test
		public void canUpdatePerson() {
			people.add(MARK);
			people.add(MARK);
			assertThat(people.size(), equalTo(3));
		}

		@Test
		public void sizeShouldBeTwo() {
			assertThat(people.size(), equalTo(2));
		}

		@Test
		public void shouldNotBeEmpty() {
			assertFalse(people.isEmpty());
		}

		@Test
		public void shouldContainPeter() {
			assertTrue(people.contains(PETER));
		}

		@Test
		public void shouldContainPaulAndPeter() {
			assertTrue(people.containsAll(asList(PAUL, PETER)));
		}
		
		@Test
		public void canRemovePeter() {
			people.remove(PETER);
			assertFalse(people.contains(PETER));
		}
		
		@Test
		public void canRemovePeterAndPaul() {
		 	people.removeAll( asList(PAUL, PETER) );
			assertFalse( people.contains( PAUL ) );
			assertFalse( people.contains( PETER ) );
		}
		
		@Test
		public void canClearAll() {
			people.clear();
			assertTrue(people.isEmpty());
		}
		
		@Test
		public void canRetainAll() {
			people.retainAll( asList(PAUL) );	
			assertTrue(people.contains(PAUL));
		}
		

	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithSeveralPeopleFilteredByNameLikeFoo {
		
		private Person jim = new Person("Jim");
		private Person paul = new Person("Paul");
		private Person peter = new Person("Peter");
		private Person james = new Person("James");
		private Person gabrial = new Person("Gabrial");
		private Person pearson = new Person("Pearson");
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private ActiveSet<Person> filteredPeople;
		
		private ActiveSet<Person> people;
		
		@Before
		public void context() {
			
			people = new People(entityManagerFactory);
			
			people.add(jim);
			people.add(paul);
			people.add(peter);
			people.add(james);
			
			this.filteredPeople = people.where("name like 'P%'");
		}
		
		@Test
		public void sizeIsFiltered() {
			assertThat(filteredPeople.size(), equalTo(2));
		}
		
		@Test
		public void isEmptyIsFalse() {
			assertFalse(filteredPeople.isEmpty());
		}
		
		@Test
		public void containsEntityMatchingFilter() {
			assertTrue(filteredPeople.contains(paul));
		}
		
		@Test
		public void doesNotContainEntityNotMatchingFilter() {
			assertFalse(filteredPeople.contains(jim));
		}
		
		@Test
		public void iteratorIsFiltered() {
			
			Set<Person> people = new HashSet<Person>(filteredPeople);
			
			assertFalse(people.containsAll(Arrays.asList(jim, james)));
			assertTrue(people.containsAll(Arrays.asList(paul, peter)));
		}
		
		@Test
		public void addedEntitiesAreFiltered() {
			
			filteredPeople.add(gabrial);
			
			assertFalse(filteredPeople.contains(gabrial));
		}
		
		@Test
		public void addedEntitesAreContainedInUnderlyingSet() {
			
			filteredPeople.add(pearson);
			
			assertTrue(filteredPeople.contains(pearson));
		}
		
		@Test
		public void filteredOutEntitiesCanNotBeRemoved() {
			
			filteredPeople.remove(jim);
			
			assertFalse(filteredPeople.contains(jim));
			assertTrue(people.contains(jim));
			
		}
		
		@Test
		public void filteredInEntitiesCanBeRemoved() {
			
			filteredPeople.remove(paul);
			
			assertFalse(filteredPeople.contains(paul));
			assertFalse(people.contains(paul));
			
		}
		
		@Test
		public void canClear() {
			
			Set<Person> beforeClear = new HashSet<Person>(filteredPeople);
			
			filteredPeople.clear();
			
			assertTrue(filteredPeople.isEmpty());
			assertFalse(people.containsAll(beforeClear));
			
		}
		
		@Test
		public void clearDoesNotRemoveEntitiesThatDoNotMatchFilter() {
			
			filteredPeople.clear();
			assertTrue(people.contains(jim));
			
		}
		
	}

}
