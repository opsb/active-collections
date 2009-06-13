package com.googlecode.activecollections;

import static com.googlecode.activecollections.ActiveSet.activeSet;
import static com.googlecode.activecollections.examples.PersonStubs.paul;
import static com.googlecode.activecollections.examples.PersonStubs.peter;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.googlecode.activecollections.examples.Person;

@RunWith(Enclosed.class)
public class InMemoryActiveSetSpec {

	public static class EmptySet {
		
		private static final Long ID = 1L;
		private ActiveSet<Person> people;
		
		@Before
		public void context() {
			people = activeSet(new HashSet<Person>());
		}
		
		@Test
		public void shouldHaveOneMorePersonAfterAdd() {
			people.add(paul());
			assertThat(people.size(), equalTo(1));
		}
		
		@Test
		public void shouldHaveTwoMorePeopleAfterAddingTwo() {
			people.addAll(asList(paul(), peter()));
		}
		
		@Test(expected=NotImplementedException.class)
		public void shouldHaveUnimplementedFind() {
			people.find(ID);
		}
		
		@Test(expected=NotImplementedException.class)
		public void shouldHaveUnimplementedFindOrNull() {
			people.findOrNull(ID);
		}
		
		@Test
		public void isEmpty() {
			assertTrue(people.isEmpty());
		}
		
		@Test
		public void shouldHaveIteratorWithNoItems() {
			assertFalse(people.iterator().hasNext());
		}
		
		@Test
		public void shouldHaveOneMorePersonAfterSave() {
			people.save(paul());
			assertThat(people.size(), equalTo(1));
		}
		
		@Test
		public void shouldHaveSizeOfZero() {
			assertThat(people.size(), equalTo(0));
		}
		
	}
	
	public static class WithOnePerson {
		
		private final Person PERSON = paul();
		
		private ActiveSet<Person> people;
		
		@Before
		public void context() {
			people = activeSet(PERSON);
		}
		
		@Test
		public void shouldBeEmptyAfterClear() {
			people.clear();
			assertTrue(people.isEmpty());
		}
		
		@Test
		public void shouldContainPerson() {
			people.contains(PERSON);
		}
		
		@Test
		public void shouldBeEmptyAfterRemove() {
			people.remove(PERSON);
			assertTrue(people.isEmpty());
		}
		
		@Test
		public void shouldStillContainPersonAfterRetainAll() {
			people.retainAll(asList(PERSON));
			assertTrue(people.contains(PERSON));
		}
		
		@Test
		public void shouldProduceAnArrayOfObjectsContainingPerson() {
			assertThat(people.toArray(), hasItemInArray((Object)PERSON));
		}
		
		@Test
		public void shouldProduceAnArrayOfPeopleContainingPerson() {
			assertThat(people.toArray(new Person[]{}), hasItemInArray(PERSON));
		}
		
		@Test(expected=NotImplementedException.class)
		public void shouldNotImplementWhere() {
			people.where("");
		}
		
	}
	
	public static class WithTwoPeople {
		
		private Person person1 = paul();
		private Person person2 = peter();
		
		private Collection<Person> allPeople = Arrays.asList(person1, person2);
		
		private ActiveSet<Person> people;
		
		@Before
		public void context() {
			people = activeSet(person1, person2);
		}
		
		@Test
		public void shouldBeEmptyAfterRemovingTwoPeople() {
			people.removeAll(allPeople);
			assertTrue(people.isEmpty());
		}
		
		@Test
		public void shouldContainAllPeople() {
			assertTrue(people.containsAll(allPeople));
		}
		
		@Test
		public void equalToAllPeople() {
			assertTrue(people.equals(allPeople));
		}
		
		@Test
		public void shouldHaveConsistentHash() {
			
			assertThat(people.hashCode(), equalTo(people.hashCode()));
			
		}
		
	}
	
}
