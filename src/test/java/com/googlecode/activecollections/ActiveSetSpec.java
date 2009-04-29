package com.googlecode.activecollections;

import static com.googlecode.activecollections.ActiveSet.activeSet;
import static com.googlecode.activecollections.PersonStubs.paul;
import static com.googlecode.activecollections.PersonStubs.peter;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.junit.Test;

public class ActiveSetSpec {

	@Test
	public void shouldCreateActiveSetForJpa() {
		assertNotNull(activeSet(Person.class, mock(EntityManagerFactory.class)));
	}
	
	@Test
	public void shouldCreateActiveSetFromHashSet() {
		
		Set<Person> people = new HashSet<Person>(asList(paul(), peter()));
		assertNotNull(activeSet(people));
		
	}
	
	@Test
	public void shouldCreateActiveSetFromArrayOfEntities() {
		assertNotNull(activeSet(paul(), peter()));
	}
	
}
