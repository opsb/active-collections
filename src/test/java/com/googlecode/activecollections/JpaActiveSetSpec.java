package com.googlecode.activecollections;

import static com.googlecode.activecollections.examples.PersonStubs.gabrial;
import static com.googlecode.activecollections.examples.PersonStubs.james;
import static com.googlecode.activecollections.examples.PersonStubs.jim;
import static com.googlecode.activecollections.examples.PersonStubs.jim_carrey;
import static com.googlecode.activecollections.examples.PersonStubs.jim_cramer;
import static com.googlecode.activecollections.examples.PersonStubs.mark;
import static com.googlecode.activecollections.examples.PersonStubs.paul;
import static com.googlecode.activecollections.examples.PersonStubs.pearson;
import static com.googlecode.activecollections.examples.PersonStubs.peter;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import com.googlecode.activecollections.examples.Address;
import com.googlecode.activecollections.examples.JpaPeople;
import com.googlecode.activecollections.examples.PeopleBeginningWithP;
import com.googlecode.activecollections.examples.PeopleContainingPaulAndJim;
import com.googlecode.activecollections.examples.PeopleContainingPaulAndJimWithDisabledClause;
import com.googlecode.activecollections.examples.Person;
import com.googlecode.activecollections.matchers.OrderMatcher;

@RunWith(Enclosed.class)
public class JpaActiveSetSpec {

	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class EmptyPeopleSpec {

		private final Person PAUL = paul();
		private final Person PETER = peter();

		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;

		private JpaActiveSet<Person> people;

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

		private JpaActiveSet<Person> people;

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

		private JpaActiveSet<Person> people;

		@Before
		public void context() {
			people = new JpaPeople(entityManagerFactory);
			people.add(PETER);
			people.add(PAUL);
		}

		@Test
		public void shouldChainConditions() {
			assertTrue("Should be empty", people.where("name like ?", "%p%").where("id = ?", 1L).isEmpty());
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
	public static class EmptySet {
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private JpaPeople people;
		
		@Before
		public void context() {
			people = new JpaPeople(entityManagerFactory).none();
		}
		
		@Test
		public void shouldHaveNullForFirst() {
			assertNull(people.first());
		}
		
	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithSeveralPeopleFilteredByNameLikeFoo {
		
		private static final Date START_DATE = new GregorianCalendar(2004, 3, 3).getTime();
		private static final Date END_DATE = new GregorianCalendar(2004, 10, 3).getTime();
		private static final Date BIRTHDAY = new GregorianCalendar(2004, 7, 3).getTime();
		
		private Person jim = jim();
		private Person jim_cramer = jim_cramer();
		private Person jim_carrey = jim_carrey();
		private Person paul = new Person("Paul", BIRTHDAY);
		private Person peter = peter();
		private Person james = james();
		private Person gabrial = gabrial();
		private Person pearson = pearson();
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private JpaActiveSet<Person> filteredPeople;
		
		private JpaPeople people;
		
		@Before
		public void context() {
			
			people = new JpaPeople(entityManagerFactory);
			
			people.add(jim_cramer);
			people.add(jim);
			people.add(jim_carrey);
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
		public void filteredOutEntitiesCanBeRemoved() {
			
			filteredPeople.remove(jim);
			
			assertFalse(filteredPeople.contains(jim));
			assertFalse(people.contains(jim));
			
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
		public void shouldBeAbleToMultipleOrder() {
			assertThat(people.orderedBy("name").orderedBy("surname"), OrderMatcher.orderedSameAs(asList(james, jim, jim_carrey, jim_cramer, paul, peter)));
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
			assertThat(pagedPeople.page(2).first().getName(), equalTo(paul().getName()));
		}
		
		@Test(expected=IllegalArgumentException.class)
		public void shouldNotAllowValuesLessThanOneAsPage() {
			pagedPeople.page(0);
		}
		
	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class SubclassedActiveSet {
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private PeopleBeginningWithP peopleBeginningWithP;
		
		private Set<Person> beginningWithP;
		
		private Set<Person> beginningWithJ;

		@Before
		public void context() {
			
			peopleBeginningWithP = new PeopleBeginningWithP(entityManagerFactory);
			beginningWithP = new HashSet<Person>();
			beginningWithJ = new HashSet<Person>();
			
			for(int i = 0; i < 10; i++) {
				Person person = jim();
				peopleBeginningWithP.add(person);
				beginningWithJ.add(person);
			}
			
			for(int i = 0; i < 10; i++) {
				Person person = paul();
				peopleBeginningWithP.add(person);
				beginningWithP.add(person);
			}
		}
		
		@Test
		public void shouldContainPeopleBeginningWithP() {
			assertTrue(peopleBeginningWithP.containsAll(beginningWithP));
		}
		
		@Test
		public void shouldNotContainPeopleBeginningWithJ() {
			assertFalse(peopleBeginningWithP.containsAll(beginningWithJ));
		}
		
	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class SubclassedActiveSetWithVarArgsParams {
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private PeopleContainingPaulAndJim peopleContainingPaulAndJim;
		
		private Set<Person> beginningWithP;
		
		private Set<Person> beginningWithJ;

		@Before
		public void context() {
			
			peopleContainingPaulAndJim = new PeopleContainingPaulAndJim(entityManagerFactory);
			beginningWithP = new HashSet<Person>();
			beginningWithJ = new HashSet<Person>();
			
			for(int i = 0; i < 10; i++) {
				Person person = jim();
				peopleContainingPaulAndJim.add(person);
				beginningWithJ.add(person);
			}
			
			for(int i = 0; i < 10; i++) {
				Person person = paul();
				peopleContainingPaulAndJim.add(person);
				beginningWithP.add(person);
			}
		}
		
		@Test
		public void shouldContainPauls() {
			assertTrue(peopleContainingPaulAndJim.containsAll(beginningWithP));
		}
		
		@Test
		public void shouldContainJims() {
			assertFalse(peopleContainingPaulAndJim.containsAll(beginningWithJ));
		}
		
		@Test
		public void shouldHaveJimsOnly() {
			assertFalse(peopleContainingPaulAndJim.jimOnly().containsAll(beginningWithP));
		}
	}
	

	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class SubclassedActiveSetWithVarArgsParamsAndDisabledClause {
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private PeopleContainingPaulAndJimWithDisabledClause peopleContainingPaulAndJim;
		
		private Set<Person> beginningWithP;
		
		private Set<Person> beginningWithJ;

		private Set<Person> beginningWithG;
		
		@Before
		public void context() {
			
			peopleContainingPaulAndJim = new PeopleContainingPaulAndJimWithDisabledClause(entityManagerFactory);
			beginningWithP = new HashSet<Person>();
			beginningWithJ = new HashSet<Person>();
			beginningWithG = new HashSet<Person>();
			
			for(int i = 0; i < 10; i++) {
				Person person = jim();
				peopleContainingPaulAndJim.add(person);
				beginningWithJ.add(person);
			}
			
			for(int i = 0; i < 10; i++) {
				Person person = paul();
				peopleContainingPaulAndJim.add(person);
				beginningWithP.add(person);
			}
			
			for(int i = 0; i < 10; i++) {
				Person person = gabrial();
				peopleContainingPaulAndJim.add(person);
				beginningWithG.add(person);
			}
		}
		
		@Test
		public void shouldContainGabrials() {
			assertTrue(peopleContainingPaulAndJim.containsAll(beginningWithG));
		}
		
		@Test
		public void shouldContainJims() {
			assertTrue(peopleContainingPaulAndJim.containsAll(beginningWithJ));
		}
		
		@Test
		public void shouldHaveJimsOnly() {
			assertFalse(peopleContainingPaulAndJim.jimOnly().containsAll(beginningWithP));
		}
	}
	
	@RunWith(UnitilsJUnit4TestClassRunner.class)
	@SpringApplicationContext("spring-context.xml")
	public static class WithJoins {
		
		private Person peter = peter();
		private Person jim = jim();
		private Person paul = paul();
		private Address eastStreet = new Address("East Street", 5);
		private Address westStreet = new Address("West Street", 10);
		private Address crazyStreet = new Address("Crazy Street", 12);
		
		@SpringBeanByType
		private EntityManagerFactory entityManagerFactory;
		
		private JpaPeople people;
		
		private JpaActiveSet<Address> addresses;
		
		@Before
		public void context() {
			people = new JpaPeople(entityManagerFactory);
			addresses = new JpaActiveSet<Address>(Address.class, entityManagerFactory);
			
			addresses.addAll(eastStreet, westStreet, crazyStreet);
			
			peter.setAddresses(asList(eastStreet, westStreet));
			jim.setAddresses(asList(eastStreet));		
			people.addAll(peter, jim, paul);
		}
		
		@Test
		public void everyOneWhoLivesOnEastStreet() {
			assertThat(people.join("person.addresses address").where("address.street = ?", "East Street"), Matchers.hasItems(peter, jim));
		}
		
		@Test
		public void shouldDoLeftOuterJoin() {
			assertThat(people.leftOuterJoin("person.addresses address").size(), equalTo(4));
		}
		
		@Test
		public void shouldDoRightOuterJoin() {
			assertThat(people.rightOuterJoin("person.addresses address").size(), equalTo(3));
		}
		
	}
}
