package opsb.activecollections.examples;

import javax.persistence.EntityManagerFactory;

import opsb.activecollections.JpaActiveSet;


public class JpaPeople extends JpaActiveSet<Person> {
	
	protected JpaPeople() {}
	
	public JpaPeople(EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory);
	}
	
	public JpaPeople withNameLike(String name) {
		return where("name like ?", name);
	}
	
}
