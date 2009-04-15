package com.googlecode.activecollections;

import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.googlecode.activecollections.ActiveSet;


public class People extends ActiveSet<Person> {
	
	public People(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory);
	}

}
