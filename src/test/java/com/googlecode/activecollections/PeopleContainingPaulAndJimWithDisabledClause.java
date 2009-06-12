package com.googlecode.activecollections;


import javax.persistence.EntityManagerFactory;

public class PeopleContainingPaulAndJimWithDisabledClause extends JpaActiveSet<Person> {

	PeopleContainingPaulAndJimWithDisabledClause() {}
	
	public PeopleContainingPaulAndJimWithDisabledClause(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory, "", new JimAndPaulClause(false));
	}
	
	public PeopleContainingPaulAndJimWithDisabledClause jimOnly() {
		return where("person.name in (?)", "jim");
	}
	
}
