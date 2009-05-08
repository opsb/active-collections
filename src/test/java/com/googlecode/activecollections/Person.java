package com.googlecode.activecollections;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="people")
public class Person {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@SuppressWarnings("unused")
	private Person() {} // Required by JPA

	public Person(String name) {
		this.name = name;
	}
	
	public Long getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
