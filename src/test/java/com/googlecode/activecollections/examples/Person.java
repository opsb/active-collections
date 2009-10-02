package com.googlecode.activecollections.examples;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

@Entity
@Table(name="people")
public class Person {

	@Id
	@GeneratedValue
	private Long id;

	private String name;
	
	private String surname = StringUtils.EMPTY;

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
	
	public Person(String name, String surname) {
		this(name);
		this.surname = surname;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSurname() {
		return surname;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Date getBirthday() {
		return birthday;
	}
	
	@Override
	public String toString() {
		return "Person [birthday=" + birthday + ", id=" + id + ", name=" + name
				+ ", surname=" + surname + "]";
	}

}
