package com.googlecode.activecollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.NotImplementedException;

class InMemoryActiveSet<T> extends ActiveSet<T>{

	private Set<T> items;
	
	public InMemoryActiveSet(Set<T> items) {
		this.items = items;
	}
	
	public boolean add(T o) {
		return items.add(o);
	}

	@Override
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
	public T findOrNull(Long id) {
		throw new NotImplementedException();
	}

	@Override
	public void save(T entity) {
		add(entity);
	}

	@Override
	public Integer pageSize() {
		throw new NotImplementedException();
	}

	@Override
	public <E extends ActiveSet<T>> E orderedBy(String orderClause) {
		throw new NotImplementedException();
	}

	@Override
	public <E extends ActiveSet<T>> E page(Integer page) {
		throw new NotImplementedException();
	}

	@Override
	public <E extends ActiveSet<T>> E pagesOf(Integer pageSize) {
		throw new NotImplementedException();
	}

	@Override
	public <E extends ActiveSet<T>> E where(String conditionsClause, Object... params) {
		throw new NotImplementedException();
	}

	@Override
	public <E extends ActiveSet<T>> E where(String conditionsClause, Map<String, Object> params) {
		throw new NotImplementedException();
	}

	@Override
	public <E extends ActiveSet<T>> E join(String joins) {
		throw new NotImplementedException();
	}

	@Override
	public Set<T> frozen() {
		return this;
	}

	@Override
	public T first() {
		return iterator().next();
	}

	@Override
	public Collection<T> refreshAll(Collection<T> staleEntities) {
		throw new NotImplementedException();
	}

	@Override
	public List<T> frozenList() {
		throw new NotImplementedException();
	}

	@Override
	public SortedSet<T> frozenSortedSet() {
		throw new NotImplementedException();
	}

	@Override
	public boolean addAll(T... entities) {
		throw new NotImplementedException();
	}

}
