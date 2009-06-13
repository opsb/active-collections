package com.googlecode.activecollections.examples;


import javax.persistence.EntityManagerFactory;

import com.googlecode.activecollections.JpaActiveSet;

public class PeopleContainingPaulAndJimWithDisabledClause extends JpaActiveSet<Person> {

	PeopleContainingPaulAndJimWithDisabledClause() {}
	
	public PeopleContainingPaulAndJimWithDisabledClause(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory, "", new JimAndPaulClause(false));
	}
	
	public PeopleContainingPaulAndJimWithDisabledClause jimOnly() {
		return where("person.name in (?)", "jim");
	}
	
}
