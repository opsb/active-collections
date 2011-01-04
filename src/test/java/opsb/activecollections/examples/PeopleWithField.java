package opsb.activecollections.examples;

import javax.persistence.EntityManagerFactory;

import opsb.activecollections.JpaActiveSet;

public class PeopleWithField extends JpaActiveSet<Person>{

	private String name;
	
	protected PeopleWithField() {}
	
	public PeopleWithField(String name, EntityManagerFactory entityManagerFactory) {
		super(Person.class, entityManagerFactory);
		this.name = name;
	}
	
	public PeopleWithField withNameLike(String name) {
		return where("name like ?", name);
	}
	
	public String getName() {
		return name;
	}
	
}
