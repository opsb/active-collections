package com.googlecode.activecollections;

import static com.googlecode.activecollections.PersonStubs.gabrial;
import static com.googlecode.activecollections.PersonStubs.james;
import static com.googlecode.activecollections.PersonStubs.jim;
import static com.googlecode.activecollections.PersonStubs.mark;
import static com.googlecode.activecollections.PersonStubs.paul;
import static com.googlecode.activecollections.PersonStubs.pearson;
import static com.googlecode.activecollections.PersonStubs.peter;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

@RunWith(Enclosed.class)
public class JpaActiveSetSpec {

	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class EmptyPeopleSpec {

		private final Person PAUL = paul();
		private final Person PETER = peter();

		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private ActiveSet<Person> people;

		@Before
		public void context() {
			
			people = new JpaPeople(entityManagerFactory);
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

		private static final Long INVALID_ID = 99L;
		private final Person PAUL = paul();
		private final Person PETER = peter();

		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private ActiveSet<Person> people;

		@Before
		public void context() {
			people = new JpaPeople(entityManagerFactory);
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
		
		@Test
		public void shouldFindPersonUsingId() {
			
			Person targetPerson = people.iterator().next();
			Long id = targetPerson.getId();
			
			assertThat(people.find(id), equalTo(targetPerson));
		}
		
		@Test(expected=IllegalArgumentException.class)
		public void shouldThrowExceptionWhenNoPersonIsFoundForAnId() {
			people.find(INVALID_ID);
		}
		
		@Test
		public void shouldReturnNullIfRequestedInsteadOfExceptionForUnfoundPerson() {
			assertNull(people.findOrNull(INVALID_ID));
		}
		
		@Test
		public void shouldAllowSaveAsAliasForAdd() {
			people.save(PAUL);
			assertThat(people.size(), equalTo(2));
		}

	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithTwoPeopleSpec {

		private final Person PAUL = paul();
		private final Person PETER = peter();
		private final Person MARK = mark();
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private ActiveSet<Person> people;

		@Before
		public void context() {
			people = new JpaPeople(entityManagerFactory);
			people.add(PETER);
			people.add(PAUL);
		}

		@Test
		public void shouldChainConditions() {
			assertThat(people.where("name like ?", "%p%").where("id = ?", 1L), equalTo(ActiveSet.<Person>empty()));
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
		
		
		@Test
		public void shouldBeAbleToOrder() {
			JpaPeople orderedPeople = people.orderedBy("name");
			assertThat(orderedPeople, OrderMatcher.orderedSameAs(asList(PAUL, PETER)));
		}
		

		@Test
		public void shouldBeAbleToOrderDescending() {
			assertThat(people.orderedBy("name desc"), OrderMatcher.orderedSameAs(asList(PETER, PAUL)));
		}

	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithSeveralPeopleFilteredByNameLikeFoo {
		
		private static final Date START_DATE = new GregorianCalendar(2004, 3, 3).getTime();
		private static final Date END_DATE = new GregorianCalendar(2004, 10, 3).getTime();
		private static final Date BIRTHDAY = new GregorianCalendar(2004, 7, 3).getTime();
		
		private Person jim = jim();
		private Person paul = new Person("Paul", BIRTHDAY);
		private Person peter = peter();
		private Person james = james();
		private Person gabrial = gabrial();
		private Person pearson = pearson();
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private ActiveSet<Person> filteredPeople;
		
		private JpaPeople people;
		
		@Before
		public void context() {
			
			people = new JpaPeople(entityManagerFactory);
			
			people.add(jim);
			people.add(paul);
			people.add(peter);
			people.add(james);
			
			this.filteredPeople = people.withNameLike("P%");
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
			
			assertFalse(people.containsAll(asList(jim, james)));
			assertTrue(people.containsAll(asList(paul, peter)));
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
		
		@Test
		public void shouldBeAbleToOrder() {
			assertThat(filteredPeople.orderedBy("name"), OrderMatcher.orderedSameAs(asList(paul, peter)));
		}
		

		@Test
		public void shouldBeAbleToOrderDescending() {
			assertThat(filteredPeople.orderedBy("name desc"), OrderMatcher.orderedSameAs(asList(peter, paul)));
		}
		
		@Test
		public void shouldHavePageSizeOfTwentyFive() {
			assertThat(filteredPeople.pageSize(), equalTo(25));
		}
		
		@Test
		public void shouldQueryByDateRange() {
			assertThat(filteredPeople.where("birthday between ? and ?", START_DATE, END_DATE), hasItem(paul));
		}
		
	}


	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithManyPeopleWithPageSizeOfTen {
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private JpaActiveSet<Person> pagedPeople;
		
		@Before
		public void context() {
			
			JpaPeople people = new JpaPeople(entityManagerFactory);
			
			for(int i = 0; i < 10; i++) {
				people.add(jim());
			}
			
			for(int i = 0; i < 10; i++) {
				people.add(paul());
			}
			
			for(int i = 0; i < 10; i++) {
				people.add(peter());
			}
			
			pagedPeople = people.pagesOf(10);
			
		}
		
		@Test
		public void shouldHavePagesOfTen() {
			assertThat(pagedPeople.pageSize(), equalTo(10));
		}
		
		@Test
		public void shouldOnlyHaveItemsForCurrentPage() {
			assertThat(pagedPeople, not(hasItem(paul())));
		}
		
		@Test
		public void shouldHaveNextPageContainingPaul() {
			assertThat(pagedPeople.page(1).first().getName(), equalTo(paul().getName()));
		}
		
	}

	
}
