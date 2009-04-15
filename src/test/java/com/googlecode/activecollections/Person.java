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

	private Person() {
	}

	public Person(String name) {
		this.name = name;
	}
	
	public Long getId() {
		return id;
	}

}
