package com.googlecode.activecollections;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	public abstract <E extends ActiveSet<T>> E where(String conditionsClause, Object ... params);
	
	public abstract <E extends ActiveSet<T>> E where(String conditionsClause, Map<String,Object> params);
	
	public abstract T find(Long id);
	
	public abstract T findOrNull(Long id);
	
	public abstract void save(T entity);

	public static <T>ActiveSet<T> empty() {
		return activeSet(new HashSet<T>());
	}

	public abstract <E extends ActiveSet<T>> E orderedBy(String orderClause);

	public abstract Integer pageSize();
	
	public abstract <E extends ActiveSet<T>> E pagesOf(Integer pageSize);
	
	public abstract <E extends ActiveSet<T>> E page(Integer page);

	
}