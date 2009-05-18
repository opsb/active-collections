package com.googlecode.activecollections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional(propagation=Propagation.REQUIRED)
public class JpaActiveSet<T> extends ActiveSet<T> {

	private static final Integer DEFAULT_PAGE_SIZE = 25;

	private static final int FIRST = 0;

	private Field idField;
	
	private Class<T> clazz;

	private Integer page;
	
	private String conditionsClause;
	
	private Map<String,Object> params;
	
	private String orderClause;
	
	protected EntityManagerFactory entityManagerFactory;
	
	private JpaDaoSupport jpaDaoSupport;

	private Integer pageSize = DEFAULT_PAGE_SIZE;
	
	protected JpaActiveSet() {}
	
	public JpaActiveSet(Class<T> clazz, final EntityManagerFactory entityManagerFactory, String conditionsClause, String orderClause, Map<String,Object> params) {
		
		jpaDaoSupport = new JpaDaoSupport(){{
			setEntityManagerFactory(entityManagerFactory);			
		}};
		this.entityManagerFactory = entityManagerFactory;
		
		this.clazz = clazz;
		this.conditionsClause = conditionsClause;
		this.orderClause = orderClause;
		this.idField = getIdField(clazz);
		this.params = params;
	}

	@SuppressWarnings("unchecked")
	private <E extends JpaActiveSet<T>> E copy() {
		try {
			Constructor<E> constructor = (Constructor<E>) getClass().getDeclaredConstructor();
			constructor.setAccessible(true);
			E copy = constructor.newInstance();
			
			copy.entityManagerFactory = entityManagerFactory;
			copy.jpaDaoSupport = jpaDaoSupport;
			
			return buildMeta(copy);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	private <E extends JpaActiveSet<T>> E buildMeta(E copy) {
		
		copy.clazz = clazz;
		copy.conditionsClause = conditionsClause;
		copy.orderClause = orderClause;
		copy.idField = getIdField(clazz);
		copy.params = params;
		copy.page = page;
		copy.pageSize = pageSize;
		
		return copy;
	}
	
	private String getRetainAllQuery() {
		return "delete from " + getEntityName() + " " + getReferenceName() + " where " + getReferenceName() + " not in (:entities)" + getAndClause();
	}
	
	private String getContainsAllQuery() {
		return "select count(" + getReferenceName() + ") from " + getEntityName() + " " + getReferenceName() + " where " + getReferenceName() + " in (:entities)" + getAndClause();
	}
	
	private String getAllQuery() {
		return "from " + getEntityName() + " " + getReferenceName() + getWhereClause() + (orderClause.length() == 0 ? "" : " order by " + orderClause);
	}
	
	private String getAndClause() {
		return conditionsClause.length() == 0 ? "" : " and " + conditionsClause;
	}
	
	private String getDeleteQuery() {
		return "delete from " + getEntityName() + " " + getReferenceName() + getWhereClause();
	}
	
	private String getEntityName() {
		return clazz.getSimpleName();
	}
	
	private String getWhereClause() {
		return conditionsClause.length() == 0 ? "" : " where " + conditionsClause;
	}
	
	private String getSizeQuery() {
		return "SELECT COUNT(" + getEntityName() + ") FROM " + getEntityName() + " " + getReferenceName() + getWhereClause();
	}
	
	protected JpaTemplate getJpaTemplate() {
		return jpaDaoSupport.getJpaTemplate();
	}
	
	public JpaActiveSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		this( clazz, entityManagerFactory, "", "", new HashMap<String, Object>() );
	}

	@SuppressWarnings("unchecked")
	private Field getIdField(Class<T> type) {
		
		for(Class clazz = type; clazz.getSuperclass() != null; clazz = clazz.getSuperclass()) {
			for(Field field : clazz.getDeclaredFields()) {
				for(Annotation annotation : field.getAnnotations()) {
					if (annotation instanceof Id) {
						field.setAccessible(true);
						return field;
					}
				}
			}
		}
		
		throw new IllegalArgumentException("Entity " + type.getName() + "must have a field marked with an Id annotation");
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
				Query query = em.createQuery(getDeleteQuery());
				addParamsTo(query);
				query.executeUpdate();
				return null;
			}
			
		});
	}
	
	private void addParamsTo(Query query) {
		for(Map.Entry<String, Object> entry : params.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Date) {
				query.setParameter(entry.getKey(), (Date)value, TemporalType.TIMESTAMP);
			}
			else if (value instanceof Calendar) {
				query.setParameter(entry.getKey(), (Calendar)value, TemporalType.TIMESTAMP);
			}
			else {
				query.setParameter(entry.getKey(), value);
			}
		}
	}

	public boolean containsAll(final Collection<? extends Object> entities) {
		
		for(Object entity : entities) {
			if (!isPersisted(entity)) return false;
		}
		
		return (Boolean)getJpaTemplate().execute(new JpaCallback() {
	
			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				Query query = em.createQuery(getContainsAllQuery());
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
				Query query = em.createQuery(getSizeQuery());
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
		return getJpaTemplate().executeFind(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(getAllQuery());
				addParamsTo(query);
				addPagingTo(query);
				
				return query.getResultList();
			}

			private void addPagingTo(Query query) {
				if (isPaged()) {
					query.setFirstResult(page * pageSize);
					query.setMaxResults(pageSize);
				}
			}
			
		});
	}
	
	private boolean isPaged() {
		return page != null;
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
				Query query = em.createQuery(getRetainAllQuery());
				query.setParameter("entities", entities);
				query.executeUpdate();
				return null;
			}
			
		});
		
		int sizeAfter = size();
		
		return sizeBefore != sizeAfter;
		
	}

	@Override
	public <E extends JpaActiveSet<T>> E where(String conditionsClause, Object ... params) {
		StringBuilder clauseWithNamedParams = new StringBuilder();
		Map<String,Object> namedParams = new HashMap<String, Object>();
		if (params.length > 0) {
			int fromIndex = 0;
			int endIndex = 0;
			for(Object param : params) {
				endIndex = conditionsClause.indexOf('?', fromIndex);
				clauseWithNamedParams.append(conditionsClause.substring(fromIndex, endIndex));
				String uniqueName = uniqueName();
				clauseWithNamedParams.append(":" + uniqueName);
				namedParams.put(uniqueName, param);
				fromIndex = endIndex + 1;
			}
			endIndex = conditionsClause.length();
			clauseWithNamedParams.append(conditionsClause.substring(fromIndex, endIndex));
		}
		return where(clauseWithNamedParams.toString(), namedParams);
	}
	
	@Override
	public <E extends JpaActiveSet<T>> E where(String conditionsClause, Map<String,Object> params) {
		
		Map<String,Object> allParams = new HashMap<String,Object>();
		allParams.putAll(this.params);
		allParams.putAll(params);
		
		boolean hasExistingClause = StringUtils.hasText(this.conditionsClause);
		String combinedConditionsClause = hasExistingClause ? this.conditionsClause + " and " + conditionsClause : conditionsClause;
		
		E copy = copy();
		copy.conditionsClause = combinedConditionsClause;
		copy.params = allParams;
		
		return copy;
		
	}
	
	private String uniqueName() {
		return "param" + params.size();
	}

	@Override
	public <E extends JpaActiveSet<T>> E orderedBy(String orderClause) {
		E copy = copy();
		copy.orderClause = orderClause;
		return copy;
	}

	@Override
	public T find(Long id) {
		
		T finding = findOrNull(id);
		if (finding == null) throw new IllegalArgumentException("No " + getReferenceName() + " with id " + id);
		
		return finding;
	}

	@Override
	public T findOrNull(Long id) {
		return getJpaTemplate().find(clazz, id);
	}

	@Override
	public void save(T entity) {
		add(entity);
	}

	@Override
	public Integer pageSize() {
		return pageSize;
	}

	public <E extends JpaActiveSet<T>> E pagesOf(Integer pageSize) {
		E copy = copy();
		copy.pageSize = pageSize;
		copy.page = page == null? 1 : page;
		return copy;
	}

	public <E extends JpaActiveSet<T>> E page(int page) {
		E copy = copy();
		copy.page = page;
		return copy;
	}
	
	public T first() {
		return getAll().get(FIRST);
	}
	
	@Override
	public String toString() {
		Iterator<T> iter = iterator();
		StringBuilder s = new StringBuilder();
		
		while(iter.hasNext()) {
			s.append(iter.next().toString());
		}
		
		return s.toString();
	}

	private String getReferenceName() {
		return clazz.getSimpleName().toLowerCase();
	}
	
}