package com.googlecode.activecollections;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.EntityManagerFactory;


public abstract class ActiveSet<T> implements Set<T> {

	public static <T> ActiveSet<T> activeSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		return new JpaActiveSet<T>(clazz, entityManagerFactory);
	}
	
	public static <T> ActiveSet<T> activeSet(Set<T> items) {
		return new InMemoryActiveSet<T>(items);
	}
	
	public static <T> ActiveSet<T> activeSet(T ... items) {
		return activeSet(new HashSet<T>(asList(items)));
	}
	
	public abstract <E extends ActiveSet<T>> E select(String select);
	
	public abstract <E extends ActiveSet<T>> E from(String from);
	
	public abstract <E extends ActiveSet<T>> E where(String conditionsClause, Object ... params);
	
	public abstract <E extends ActiveSet<T>> E where(String conditionsClause, Map<String,Object> params);
	
	public abstract T find(Long id);
	
	public abstract T first();
	
	public abstract T findOrNull(Long id);
	
	public abstract void save(T entity);

	public static <T>ActiveSet<T> empty() {
		return activeSet(new HashSet<T>());
	}

	public abstract <E extends ActiveSet<T>> E orderedBy(String orderClause);

	public abstract Integer pageSize();
	
	public abstract <E extends ActiveSet<T>> E pagesOf(Integer pageSize);
	
	public abstract <E extends ActiveSet<T>> E page(Integer page);

	public abstract <E extends ActiveSet<T>> E join(String joins);
	
	public abstract Set<T> frozen();

	public abstract List<T> frozenList();
	
	public abstract SortedSet<T> frozenSortedSet();
	
	public abstract Set<T> frozenSet();
	
	public abstract Collection<T> refreshAll(Collection<T> staleEntities);
	
	public abstract boolean addAll(Collection<? extends T> entities); 
	
	public abstract boolean addAll(T ... entities);
	
	public abstract <E extends ActiveSet<T>> E in(Collection<T> entities);
	
}