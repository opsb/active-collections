package com.googlecode.activecollections.examples;


import javax.persistence.EntityManagerFactory;

import com.googlecode.activecollections.JpaActiveSet;

public class PeopleContainingPaulAndJim extends JpaActiveSet<Person> {

	PeopleContainingPaulAndJim() {}
	
	public PeopleContainingPaulAndJim(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory, null, new JimAndPaulClause(true));
	}
	
	public PeopleContainingPaulAndJim jimOnly() {
		return where("person.name in (?)", "jim");
	}
	
}
