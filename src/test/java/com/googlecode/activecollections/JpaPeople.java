package com.googlecode.activecollections;

import javax.persistence.EntityManagerFactory;


public class JpaPeople extends JpaActiveSet<Person> {
	
	protected JpaPeople() {}
	
	public JpaPeople(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory);
	}

}
