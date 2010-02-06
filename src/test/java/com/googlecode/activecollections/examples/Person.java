package com.googlecode.activecollections.examples;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
	
	@ManyToMany(cascade = {CascadeType.ALL})
	private List<Address> addresses;
	
	@SuppressWarnings("unused")
	private Person() {} // Required by JPA

	public Person(String name) {
		this(name, new Date());
	}
	
	public Person(String name, Date birthday) {
		this(name, "", birthday, new ArrayList<Address>());	
	}
	
	public Person(String name, String surname) {
		this(name, surname, new Date(), new ArrayList<Address>());
	}
	
	public Person(String name, String surname, List<Address> addresses) {
		this(name, surname, new Date(), addresses);
	}
	
	private Person(String name, String surname, Date birthday, List<Address> addresses) {
		this.name = name;
		this.surname = surname;
		this.birthday = birthday;
		this.addresses = addresses;
	}
	
	public Person(String name, Address address) {
		this(name);
		addresses.add(address);
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

	public Date getBirthday() {
		return birthday;
	}
	
	public List<Address> getAddresses() {
		return addresses;
	}
	
	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
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

	
	@Override
	public String toString() {
		return "Person [birthday=" + birthday + ", id=" + id + ", name=" + name + ", surname=" + surname + "]";
	}

}
