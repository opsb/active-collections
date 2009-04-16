package com.googlecode.activecollections;

import javax.persistence.EntityManagerFactory;


public class People extends ActiveSet<Person> {
	
	public People(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory);
	}

}
