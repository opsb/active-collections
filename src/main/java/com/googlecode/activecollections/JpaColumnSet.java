package com.googlecode.activecollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

public class JpaColumnSet<T,C> implements Set<C> {

	private String column;
	
	private JpaActiveSet<T> baseSet;
	
	public JpaColumnSet(String column, JpaActiveSet<T> baseSet) {
		this.column = column;
		this.baseSet = baseSet;
	}

	public boolean add(C o) {
		throw new NotImplementedException();
	}

	public boolean addAll(Collection<? extends C> c) {
		throw new NotImplementedException();
	}

	public void clear() {
		throw new NotImplementedException();
	}

	public boolean contains(Object o) {
		throw new NotImplementedException();
	}

	public boolean containsAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	public boolean isEmpty() {
		throw new NotImplementedException();
	}

	public Iterator<C> iterator() {
		throw new NotImplementedException();
	}

	public boolean remove(Object o) {
		throw new NotImplementedException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	public int size() {
		return baseSet.size();
	}

	public Object[] toArray() {
		throw new NotImplementedException();
	}

	public <T> T[] toArray(T[] a) {
		throw new NotImplementedException();
	}

}
