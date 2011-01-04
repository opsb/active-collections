/**
 * 
 */
package opsb.activecollections.examples;

import java.util.Arrays;
import java.util.List;

import opsb.activecollections.JpaClause;

class JimAndPaulClause extends JpaClause {
	
	private boolean isEnabled;
	
	public JimAndPaulClause(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	@Override
	public String getJpa() {
		return "person.name in (?)";
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<? extends Object> getPositionalParams() {
		return Arrays.asList(Arrays.asList("jim","Paul"));
	}
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
}