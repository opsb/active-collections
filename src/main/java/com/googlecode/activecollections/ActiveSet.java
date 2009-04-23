package com.googlecode.activecollections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRED)
public class ActiveSet<T> extends JpaDaoSupport implements Set<T>{

	private Field idField;
	
	private String deleteQuery;
	
	private String containsAllQuery;
	
	private String sizeQuery;
	
	private String retainAllQuery;
	
	private String getAllQuery;
	
	private Class<T> clazz;
	
	protected EntityManagerFactory entityManagerFactory;

	private String conditionsClause;
	
	private List<Object> params;
	
	protected ActiveSet() {}
	
	public ActiveSet(Class<T> clazz, EntityManagerFactory entityManagerFactory, String conditionsClause, List<Object> params) {
		
		setEntityManagerFactory(entityManagerFactory);

		this.entityManagerFactory = entityManagerFactory;
		this.clazz = clazz;
		this.conditionsClause = conditionsClause;
		this.idField = getIdField(clazz);
		this.params = params;
		
		String entityName = clazz.getSimpleName();
		String referenceName = clazz.getSimpleName().toLowerCase();
		
		String whereClause = conditionsClause.length() == 0 ? "" : " where " + conditionsClause;
		String andClause = conditionsClause.length() == 0 ? "" : " and " + conditionsClause;
		
		getAllQuery = "from " + entityName + " " + referenceName + whereClause;
		deleteQuery = "delete from " + entityName + " " + referenceName + whereClause;
		containsAllQuery = "select count(" + referenceName + ") from " + entityName + " " + referenceName + " where " + referenceName + " in (:entities)" + andClause;
		sizeQuery = "SELECT COUNT(" + entityName + ") FROM " + entityName + " " + referenceName + whereClause;
		retainAllQuery = "delete from " + entityName + " " + referenceName + " where " + referenceName + " not in (:entities)" + andClause;
	}
	
	public ActiveSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		this( clazz, entityManagerFactory, "", new ArrayList<Object>() );
	}

	private Field getIdField(Class<T> type) {
		
		for(Field field : type.getDeclaredFields()) {
			for(Annotation annotation : field.getAnnotations()) {
				if (annotation instanceof Id) {
					field.setAccessible(true);
					return field;
				}
			}
		}
		
		throw new IllegalArgumentException("Entity must have a field marked with an Id annotation");
	}

	private Object getId(Object entity) {
		try {
			return idField.get(entity);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean isPersisted(Object entity) {
		return getId(entity) != null;
	}
	
	public boolean add(T entity) {
		if (isPersisted(entity)) {
			getJpaTemplate().merge(entity);
		}
		else {
			getJpaTemplate().persist(entity);
		}
		return false;
	}

	public boolean addAll(Collection<? extends T> entities) {
		
		for(T entity : entities) {
			add(entity);
		}
		
		boolean hasAddedEntities = !entities.isEmpty(); 
		
		return hasAddedEntities; 
	}

	public void clear() {
		getJpaTemplate().execute(new JpaCallback() {
	
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(deleteQuery);
				addParamsTo(query);
				query.executeUpdate();
				return null;
			}
			
		});
	}
	
	private void addParamsTo(Query query) {
		for(int i = 0; i < params.size(); i++) {
			query.setParameter(i, params.get(i));
		}
	}

	public boolean containsAll(final Collection<? extends Object> entities) {
		
		for(Object entity : entities) {
			if (!isPersisted(entity)) return false;
		}
		
		return (Boolean)getJpaTemplate().execute(new JpaCallback() {
	
			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				Query query = em.createQuery(containsAllQuery);
				query.setParameter("entities", entities);
				addParamsTo(query);
				Long withMatchingIds = (Long) query.getSingleResult();
				
				return withMatchingIds == entities.size();
			}
			
		});
		
		
	}

	public boolean contains(Object entity) {
		return containsAll(Arrays.asList(entity));
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int size() {
		
		return ((Long)getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(sizeQuery);
				addParamsTo(query);
				return query.getSingleResult();
			}
			
		})).intValue();
		
	}

	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	public Object[] toArray() {
		return getAll().toArray();
	}

	@SuppressWarnings("unchecked")
	private List<T> getAll() {
		return getJpaTemplate().find(getAllQuery, params.toArray());
	}

	public <AT> AT[] toArray(AT[] a) {
		return getAll().toArray(a);
	}

	public boolean remove(Object entity) {
		
		int sizeBefore = size();
		
		if (contains(entity)) getJpaTemplate().remove(entity);
		
		int sizeAfter = size();
		
		return sizeAfter == sizeBefore - 1;
	}

	public boolean removeAll(Collection<?> entities) {
		
		for(Object entity : entities) {
			remove(entity);
		}
		
		boolean hasRemovedEntities = !entities.isEmpty();
		
		return hasRemovedEntities;
		
	}

	public boolean retainAll(final Collection<?> entities) {
		
		int sizeBefore = size();
		
		getJpaTemplate().execute( new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(retainAllQuery);
				query.setParameter("entities", entities);
				query.executeUpdate();
				return null;
			}
			
		});
		
		int sizeAfter = size();
		
		return sizeBefore != sizeAfter;
		
	}

	public ActiveSet<T> where(String conditionsClause, Object ... params) {
		
		List<Object> allParams = new ArrayList<Object>();
		allParams.addAll(this.params);
		allParams.addAll(Arrays.asList(params));
		
		return new ActiveSet<T>( clazz, entityManagerFactory, this.conditionsClause + " " + conditionsClause, allParams );
	}

	public T find(Long id) {
		return getJpaTemplate().find(clazz, id);
	}
	
}