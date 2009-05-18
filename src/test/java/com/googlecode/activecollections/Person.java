package com.googlecode.activecollections;

import java.util.Date;

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

	private Date birthday;
	
	@SuppressWarnings("unused")
	private Person() {} // Required by JPA

	public Person(String name) {
		this(name, new Date());
	}
	
	public Person(String name, Date birthday) {
		this.name = name;
		this.birthday = birthday;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Date getBirthday() {
		return birthday;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
