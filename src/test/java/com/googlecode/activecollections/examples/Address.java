package com.googlecode.activecollections.examples;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="address")
public class Address {
	
	@Id
	@GeneratedValue
	private Long id;

	private String street;
	
	private int number;
		
	@SuppressWarnings("unused")
	private Address() {} // Required by JPA

	public Address(String street, int number) {
		this.street = street;
		this.number = number;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getStreet() {
		return street;
	}
	
	public int getNumber() {
		return number;
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
		Address other = (Address) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Address [street=" + street + ", " + "number=" + number + "]";
	}
}
