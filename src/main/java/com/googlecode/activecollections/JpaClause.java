package com.googlecode.activecollections;

import java.util.Map;

public class JpaClause {

	private String jpa;
	
	private Map<String,Object> params;
	
	public JpaClause(String jpa, Map<String, Object> params) {
		this.jpa = jpa;
		this.params = params;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public String getJpa() {
		return jpa;
	}

}
