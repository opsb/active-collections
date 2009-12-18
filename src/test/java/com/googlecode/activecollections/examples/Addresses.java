package com.googlecode.activecollections.examples;

import javax.persistence.EntityManagerFactory;

import com.googlecode.activecollections.JpaActiveSet;

public class Addresses extends JpaActiveSet<Address>{

	public Addresses() {}
	public Addresses(EntityManagerFactory emf) {
		super(Address.class, emf);
	}
	public Addresses inLondon() {
		return where("address.city like ?", "Lonzzdon");
	}
	
}
