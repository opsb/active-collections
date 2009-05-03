/**
 * 
 */
package com.googlecode.activecollections;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;



public class OrderMatcher<T> extends TypeSafeMatcher<Collection<T>>{

	private Collection<T> inExpectedOrder;
	
	public OrderMatcher(Collection<T> inExpectedOrder) {
		this.inExpectedOrder = inExpectedOrder;
	}
	
	@Override
	public boolean matchesSafely(Collection<T> inActualOrder) {
		
		Iterator<?> actualIter = inActualOrder.iterator();
		Iterator<?> expectedIter = inExpectedOrder.iterator();
		
		while(actualIter.hasNext()) {
			if (!expectedIter.hasNext()) return false;
			if (!actualIter.next().equals(expectedIter.next())) return false;
		}
		
		return true;
	}

	public void describeTo(Description description) {
		description.appendValueList("", ",", "", inExpectedOrder);
	}
	
	public static <T> OrderMatcher<T> orderedSameAs(Collection<T> inExpectedOrder) {
		return new OrderMatcher<T>(inExpectedOrder);
	}
	
}