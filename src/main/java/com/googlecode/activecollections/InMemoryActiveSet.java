package com.googlecode.activecollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

class InMemoryActiveSet<T> extends ActiveSet<T>{

	private Set<T> items;
	
	public InMemoryActiveSet(Set<T> items) {
		this.items = items;
	}
	
	public boolean add(T o) {
		return items.add(o);
	}

	public boolean addAll(Collection<? extends T> c) {
		return items.addAll(c);
	}

	public void clear() {
		items.clear();
	}

	public boolean contains(Object o) {
		return items.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return items.containsAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Collection)) return false;
		Collection<T> collection = (Collection)o;
		return containsAll(collection);
	}

	@Override
	public int hashCode() {
		return items.hashCode();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public Iterator<T> iterator() {
		return items.iterator();
	}

	public boolean remove(Object o) {
		return items.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return items.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return items.retainAll(c);
	}

	public int size() {
		return items.size();
	}

	public Object[] toArray() {
		return items.toArray();
	}

	public <E> E[] toArray(E[] a) {
		return items.toArray(a);
	}

	@Override
	public T find(Long id) {
		throw new NotImplementedException();
	}

	@Override
	public ActiveSet<T> where(String conditionsClause, Object... params) {
		throw new NotImplementedException();
	}

	@Override
	public T findOrNull(Long id) {
		throw new NotImplementedException();
	}

	@Override
	public void save(T entity) {
		add(entity);
	}
	
}
