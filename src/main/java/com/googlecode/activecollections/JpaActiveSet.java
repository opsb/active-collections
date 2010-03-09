package com.googlecode.activecollections;


import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javassist.Modifier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.log4j.Logger;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Transactional(propagation = Propagation.REQUIRED)
public class JpaActiveSet<T> implements Set<T> {

	private Logger logger = Logger.getLogger(JpaActiveSet.class);

	private static final Integer DEFAULT_PAGE_SIZE = 25;

	private Field idField;

	private Class<T> clazz;

	private Integer page;

	private Integer pageSize = DEFAULT_PAGE_SIZE;
	
	private Integer numberOfItems;

	private JpaDaoSupport jpaDaoSupport;

	protected EntityManagerFactory entityManagerFactory;
	
	private ASQuery asQuery;

	protected JpaActiveSet() {
	}
	
	public JpaActiveSet(Class<T> clazz, final EntityManagerFactory entityManagerFactory,
			String selectClause, String fromClause, List<String> joins, List<JpaClause> conditions, List<String> orderClauses) {
		
		Assert.notNull(entityManagerFactory,
		"Can not create a JpaActiveSet without an EntityManagerFactory, was given null");
		Assert.notNull(clazz, "Must specify a class");
		
		this.clazz = clazz;
		jpaDaoSupport = new JpaDaoSupport() {
			{
				setEntityManagerFactory(entityManagerFactory);
			}
		};
		this.idField = getIdField(clazz);

		asQuery = new ASQuery(logger, getReferenceName(), getEntityName(), selectClause, fromClause, joins, conditions, orderClauses);
		
	}
	
	public JpaActiveSet(Class<T> clazz, final EntityManagerFactory entityManagerFactory, List<String> orderClauses,
			JpaClause... conditions) {
		this(clazz, entityManagerFactory, null, null, new ArrayList<String>(), asList(conditions), orderClauses);
	}

	private static <X> List<X> asList(X ... items) {
		return new ArrayList<X>(Arrays.asList(items));
	}
	
	public JpaActiveSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		this(clazz, entityManagerFactory, new ArrayList<String>());
	}
	
	public boolean add(T entity) {
		if (isPersisted(entity)) {
			getJpaTemplate().merge(entity);
			return false;
		} else {
			getJpaTemplate().persist(entity);
			return true;
		}
	}
	
	public boolean addAll(Collection<? extends T> entities) {
		
		for (T entity : entities) {
			add(entity);
		}
		
		boolean hasAddedEntities = !entities.isEmpty();
		
		return hasAddedEntities;
	}
	
	public boolean addAll(T... entities) {
		return addAll(Arrays.asList(entities));
	}
	
	public void clear() {
		removeAll(this);
	}
	
	public boolean containsAll(final Collection<? extends Object> entities) {

		if (entities == null || entities.isEmpty())
			return false;

		for (Object entity : entities) {
			if (!isPersisted(entity))
				return false;
		}

		return (Boolean) getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				ASQuery withDefaults = always().asQuery;
				
				Query query = em.createQuery(withDefaults.getContainsAllQuery());
				query.setParameter("entities", entities);
				withDefaults.addParamsTo(query);
				
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
		int total = total();
		return total > pageSize && isLimited() ? pageSize : total;
	}
	
	public int total() {
		return ((Long)getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				ASQuery withDefaults = always().asQuery;
				
				Query query = em.createQuery(withDefaults.getSizeQuery());
				withDefaults.addParamsTo(query);
				return query.getSingleResult();
			}

		})).intValue();
	}

	public Iterator<T> iterator() {
		return getAllWithCallbacks().iterator();
	}

	public Object[] toArray() {
		return getAllWithCallbacks().toArray();
	}

	public T first() {
		Collection<T> all = first(1);
		return all.isEmpty() ? null : all.iterator().next();
	}	
	
	@SuppressWarnings("unchecked")
	protected <E extends JpaActiveSet<T>> E copy() {
		try {
			Constructor<E> constructor = (Constructor<E>) getClass().getDeclaredConstructor();
			constructor.setAccessible(true);
			E copy = constructor.newInstance();

			copy.entityManagerFactory = entityManagerFactory;
			copy.jpaDaoSupport = jpaDaoSupport;

			addMeta(copy);
			afterCopy(copy);

			return copy;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected <E extends JpaActiveSet<T>> void afterCopy(E copy) {
		
		for(Field field : getDeclaredFieldsFor()) {
			try {
				if (!isStaticOrFinal(field)) {
					field.setAccessible(true);
					Object value = field.get(this);
					field.set(copy, value);
				}
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private Set<Field> getDeclaredFieldsFor() {
		Set<Field> fields = new HashSet<Field>();
		
		Class<?> clazz = getClass();
		boolean hasMoreSubclassesOfJpaActiveSet = JpaActiveSet.class != clazz;
		
		while(hasMoreSubclassesOfJpaActiveSet) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
			hasMoreSubclassesOfJpaActiveSet = JpaActiveSet.class != clazz;
		}
		
		return fields;
	}

	private boolean isStaticOrFinal(Field field) {
		int modifiers = field.getModifiers();
		return Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers);
	}

	private <E extends JpaActiveSet<T>> void addMeta(E copy) {

		copy.clazz = clazz;
		copy.idField = getIdField(clazz);
		copy.page = page;
		copy.pageSize = pageSize;
		copy.asQuery = asQuery.copy();

	}

	private String getEntityName() {
		return clazz.getSimpleName();
	}

	protected JpaTemplate getJpaTemplate() {
		return jpaDaoSupport.getJpaTemplate();
	}

	@SuppressWarnings("unchecked")
	private Field getIdField(Class<T> type) {

		for (Class clazz = type; clazz.getSuperclass() != null; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				for (Annotation annotation : field.getAnnotations()) {
					if (annotation instanceof Id) {
						field.setAccessible(true);
						return field;
					}
				}
			}
		}

		throw new IllegalArgumentException("Entity " + type.getName()
				+ "must have a field marked with an Id annotation");
	}

	private Object getId(Object entity) {
		try {
			return idField.get(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isPersisted(Object entity) {
		return getId(entity) != null;
	}


	private void addLimitsTo(Query query) {
		
		Integer start = null;
		Integer maxResults = null; 
		
		if (isLimited()) {
			int page = this.page == null ? 1 : this.page;
			
			start = (page - 1) * pageSize;
			maxResults = pageSize;

			if (numberOfItems != null) {
				if (numberOfItems < maxResults) {
					maxResults = numberOfItems;
				}
			}
			
			Assert.isTrue(start >= 0, "Invalid start row: " + start );
			Assert.isTrue(maxResults > 0, "Invalid max results: " + maxResults);
			
			query.setFirstResult(start);
			query.setMaxResults(maxResults);
		}
	}

	
	
	protected void afterLoad(T entity) {}

	private List<T> getAllWithCallbacks() {
		List<T> all = getAll();		
		for(T entity : all) afterLoad(entity);
		return all;
		
	}
	
	@SuppressWarnings("unchecked")
	private <E> List<E> getAll() {
		return (List<E>)getJpaTemplate().executeFind(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				ASQuery withDefaults = always().asQuery;
				
				Query query = em.createQuery(withDefaults.getAllQuery());
				withDefaults.addParamsTo(query);
				addLimitsTo(query);

				return query.getResultList();
			}

		});
	}

	private boolean isLimited() {
		return page != null || numberOfItems != null;
	}

	public <AT> AT[] toArray(AT[] a) {
		return getAllWithCallbacks().toArray(a);
	}

	public boolean remove(final Object entity) {

		int sizeBefore = size();

		getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Object condemned = em.find(clazz, getId(entity));
				em.remove(condemned);
				return null;
			}
			
		});

		int sizeAfter = size();

		return sizeAfter == sizeBefore - 1;
	}

	public boolean removeAll(Collection<?> entities) {

		for (Object entity : entities) {
			remove(entity);
		}

		boolean hasRemovedEntities = !entities.isEmpty();

		return hasRemovedEntities;

	}

	public boolean retainAll(final Collection<?> entities) {

		int sizeBefore = size();

		getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				Query query = em.createQuery(asQuery.getRetainAllQuery());
				query.setParameter("entities", entities);
				query.executeUpdate();
				return null;
			}

		});

		int sizeAfter = size();

		return sizeBefore != sizeAfter;

	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E where(String conditionsClause, Object... params) {

		return (E) where(new JpaClause(conditionsClause, params));

	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E and(String conditionsClause, Object... params) {
		return (E)where(conditionsClause, params);
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E where(JpaClause clause) {
		Assert.notNull(clause, "Clause was null");
		
		JpaActiveSet<T> copy = copy();
		copy.asQuery.add(clause);

		return (E) copy;

	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E and(JpaClause clause) {
		return (E)where(clause);
	}
	
	@SuppressWarnings("unchecked")
	public <F, E extends JpaActiveSet<T>> E where(JpaActiveSet<F> activeSet) {
		
		JpaActiveSet<T> copy = copy();
		copy.asQuery.merge(activeSet.asQuery);
		
		return (E)copy;
	}
	
	@SuppressWarnings("unchecked")
	public <F, E extends JpaActiveSet<T>> E and(JpaActiveSet<F> activeSet) {
		return (E)where(activeSet);
	}
	
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E ignoring(JpaClause ignoredClause) {
		Assert.notNull( ignoredClause, "Clause to ignore was null" );
		
		JpaActiveSet<T> copy = copy();
		if ( copy.asQuery.remove(ignoredClause) == false) {
			throw new IllegalArgumentException("This clause was not found: " + ignoredClause);
		}
		return (E)copy;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E join(String join) {
		
		Assert.notNull(join, "Join was null");
		
		JpaActiveSet<T> copy = copy();
		copy.asQuery.join("join", join);
		
		return (E) copy;
		
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E leftOuterJoin(String join) {
		
		Assert.notNull(join, "Left outer join was null");
		
		JpaActiveSet<T> copy = copy();
		copy.asQuery.join("left outer join", join);
		
		return (E) copy;

	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E rightOuterJoin(String join) {
		Assert.notNull(join, "Right outer join was null");

		JpaActiveSet<T> copy = copy();
		copy.asQuery.join("right outer join ", join);
		
		return (E) copy;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E orderedBy(String orderClause) {
		
		Assert.notNull(orderClause, "Order clause was null");
		
		JpaActiveSet<T> copy = copy();
		copy.asQuery.addOrderClause(orderClause);

		return (E) copy;
	}

	public T find(Object id) {

		T finding = findOrNull(id);
		if (finding == null)
			throw new IllegalArgumentException("No " + getReferenceName() + " with id " + id);

		return finding;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E find(Collection<? extends Object> ids) {
		if (ids.isEmpty()) return (E)none();
		return (E) where(getReferenceName() + "." + getIdReferenceName() + " in (?)", ids);
	}

	public T findOrNull(Object id) {
		return getJpaTemplate().find(clazz, id);
	}

	public void save(T entity) {
		add(entity);
	}

	public Integer pageSize() {
		return pageSize;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E pagesOf(Integer pageSize) {
		if (pageSize == null)
			return (E) this;
		JpaActiveSet<T> copy = copy();
		copy.pageSize = pageSize;
		copy.page = page == null ? 1 : page;
		return (E) copy;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E first(Integer numberOfItems) {
		JpaActiveSet<T> copy = copy();
		copy.numberOfItems = numberOfItems;
		return (E) copy;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E page(Integer page) {
		if (page == null)
			return (E) this;
		if (page < 1)
			throw new IllegalArgumentException("Page numbers start at 1");
		JpaActiveSet<T> copy = copy();
		copy.page = page;
		return (E) copy;
	}

	@Override
	public String toString() {
		Iterator<T> iter = iterator();
		StringBuilder s = new StringBuilder();
		while (iter.hasNext()) {
			s.append(iter.next().toString());
			if (iter.hasNext()) {
				s.append(", ");
			}
		}

		return s.toString();
	}

	@SuppressWarnings("unchecked")
	protected <E extends JpaActiveSet<T>> E all() {
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	protected <E extends JpaActiveSet<T>> E none() {
		return (E) where("true = false");
	}

	public Set<T> frozen() {
		return new LinkedHashSet<T>(this);
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E in(Collection<T> entities) {
		if (entities == null || entities.isEmpty())
			return (E) none();
		return (E) where(getReferenceName() + " in (?)", entities);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E in(T ... entities) {
		return (E)in(asList(entities));
	}

	public List<T> frozenList() {
		return new ArrayList<T>(this);
	}

	public SortedSet<T> frozenSortedSet() {
		return new TreeSet<T>(this);
	}
	
	@SuppressWarnings("unchecked")
	public Set<T> frozenOrderedSet() {
		ListOrderedSet orderedSet = new ListOrderedSet();
		orderedSet.addAll(this);
		return orderedSet;
	}

	public Set<T> frozenSet() {
		return new HashSet<T>(this);
	}

	public Collection<T> refreshAll(Collection<T> staleEntities) {
		if (CollectionUtils.isEmpty(staleEntities))
			return staleEntities;
		return where(getReferenceName() + " in (?)", staleEntities);
	}

	public String getReferenceName() {
		return clazz.getSimpleName().toLowerCase();
	}

	protected String getIdReferenceName() {
		return getIdField(clazz).getName();
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E from(String from) {
		JpaActiveSet<T> copy = copy();
		copy.asQuery.from(from);
		
		return (E) copy;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E select(String select) {
		JpaActiveSet<T> copy = copy();
		copy.asQuery.select(select);
		
		return (E) copy;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E distinct() {
		JpaActiveSet<T> copy = copy();
		copy.asQuery.distinct();
		return (E)copy;
	}
	
	@SuppressWarnings("unchecked")
	public <E> List<E> reduceToList(String property) {
		JpaActiveSet<T> copy = copy().select(getReferenceName() + "." + property);
		
		return (List<E>)copy.getAll();
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
	protected <E extends JpaActiveSet<T>> E always() {
		return (E)this;
	}

}
