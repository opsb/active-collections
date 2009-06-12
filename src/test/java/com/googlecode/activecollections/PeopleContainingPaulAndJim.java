package com.googlecode.activecollections;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManagerFactory;

public class PeopleContainingPaulAndJim extends JpaActiveSet<Person> {

	PeopleContainingPaulAndJim() {}
	
	public PeopleContainingPaulAndJim(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory, "", new JimAndPaulClause());
	}
	
	private static class JimAndPaulClause extends JpaClause {
		
		public String getJpa() {
			return "person.name in (?)";
		}
		
		@SuppressWarnings("unchecked")
		public List<? extends Object> getPositionalParams() {
			return Arrays.asList(Arrays.asList("jim","Paul"));
		}
		
	}
	
	public PeopleContainingPaulAndJim jimOnly() {
		return where("person.name in (?)", "jim");
	}
	
}
