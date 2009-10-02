package com.googlecode.activecollections.examples;

import javax.persistence.EntityManagerFactory;

import com.googlecode.activecollections.JpaActiveSet;
import com.googlecode.activecollections.JpaClause;

public class PeopleBeginningWithP extends JpaActiveSet<Person>{

	PeopleBeginningWithP() {}
	
	public PeopleBeginningWithP(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory, null, new JpaClause() {
			@Override
			public String getJpa() {
				return "person.name like 'P%'";
			}
		});
	}
	
}
