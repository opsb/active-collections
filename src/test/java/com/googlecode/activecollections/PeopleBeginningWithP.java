package com.googlecode.activecollections;

import javax.persistence.EntityManagerFactory;

public class PeopleBeginningWithP extends JpaActiveSet<Person>{

	PeopleBeginningWithP() {}
	
	public PeopleBeginningWithP(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory, "", new JpaClause() {
			@Override
			public String getJpa() {
				return "person.name like 'P%'";
			}
		});
	}
	
}
