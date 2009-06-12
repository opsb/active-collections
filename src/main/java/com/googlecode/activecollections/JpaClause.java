package com.googlecode.activecollections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaClause {

	private String jpa = "";
	
	private Map<String,Object> namedParams = new HashMap<String, Object>();

	private List<Object> positionalParams = new ArrayList<Object>();
	
	public JpaClause() {
	}
	
	public JpaClause(String jpa, Map<String, Object> namedParams, Object ... positionalParams) {
		this.jpa = jpa;
		this.namedParams = namedParams;
		this.positionalParams = Arrays.asList(positionalParams);
	}

	public JpaClause(String jpa, Object ... positionalParams) {
		this.jpa = jpa;
		this.positionalParams = Arrays.asList(positionalParams);
	}
	
	public Map<String, Object> getNamedParams() {
		return namedParams;
	}

	public List<? extends Object> getPositionalParams() {
		return positionalParams;
	}
	
	public String getJpa() {
		return jpa;
	}
	
	public boolean isEnabled() {
		return true;
	}

}
