package com.googlecode.activecollections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

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

	private final Logger logger = Logger.getLogger(JpaActiveSet.class);

	private static final String NO_ORDER_SPECIFIED = "";

	private static final Integer DEFAULT_PAGE_SIZE = 25;

	private Field idField;

	private Class<T> clazz;

	private Integer page;

	private List<JpaClause> conditionsClauses = new ArrayList<JpaClause>();

	private String orderClause;

	protected EntityManagerFactory entityManagerFactory;

	private JpaDaoSupport jpaDaoSupport;

	private Integer pageSize = DEFAULT_PAGE_SIZE;

	private List<String> joinsClauses = new ArrayList<String>();

	private String fromClause;

	private String selectClause;

	protected JpaActiveSet() {
	}

	public JpaActiveSet(Class<T> clazz, final EntityManagerFactory entityManagerFactory,
			String selectClause, String fromClause, List<String> joins, List<JpaClause> conditions, String orderClause) {
		
		Assert.notNull(entityManagerFactory,
		"Can not create a JpaActiveSet without an EntityManagerFactory, was given null");
		Assert.notNull(clazz, "Must specify a class");
		
		this.clazz = clazz;
		jpaDaoSupport = new JpaDaoSupport() {
			{
				setEntityManagerFactory(entityManagerFactory);
			}
		};
		this.selectClause = selectClause;
		this.fromClause = fromClause;
		this.joinsClauses = joins;
		this.conditionsClauses = conditions;
		this.orderClause = orderClause;
		this.idField = getIdField(clazz);
		
	}
	
	public JpaActiveSet(Class<T> clazz, final EntityManagerFactory entityManagerFactory, String orderClause,
			JpaClause... conditions) {
		this(clazz, entityManagerFactory, null, null, new ArrayList<String>(), Arrays.asList(conditions), orderClause);
	}

	public JpaActiveSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		this(clazz, entityManagerFactory, "");
	}
	
	public static <T extends Object> JpaActiveSet<T> activeSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		return new JpaActiveSet<T>(clazz, entityManagerFactory);
	}

	@SuppressWarnings("unchecked")
	private <E extends JpaActiveSet<T>> E copy() {
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
	}

	private <E extends JpaActiveSet<T>> void addMeta(E copy) {

		Assert.notNull(this.conditionsClauses);
		
		copy.clazz = clazz;
		copy.fromClause = fromClause;
		copy.selectClause = selectClause;
		copy.conditionsClauses = new ArrayList<JpaClause>(this.conditionsClauses);
		copy.orderClause = orderClause;
		copy.joinsClauses = new ArrayList<String>(joinsClauses);
		copy.idField = getIdField(clazz);
		copy.page = page;
		copy.pageSize = pageSize;

	}

	private String getFromClause() {
		return "from " + (fromClause == null ? getEntityName() + " " + getReferenceName() : fromClause);
	}

	private String getSelectClause() {
		return "select " + (selectClause == null ? getReferenceName() : selectClause);
	}

	private String getSelectCountClause() {
		return "select count(" + (selectClause == null ? getReferenceName() : selectClause) + ")";
	}

	private String getTablesClause() {
		return getFromClause() + getJoinClause();
	}

	private String getDeleteClause() {
		return "delete";
	}

	private String getRetainAllQuery() {
		String retainAllQuery = buildQuery(getDeleteClause(), " where " + getReferenceName() + " not in (:entities)"
				+ getAndClause(), NO_ORDER_SPECIFIED);
		logger.debug("retainAll query: " + retainAllQuery);
		return retainAllQuery;
	}

	private String getContainsAllQuery() {
		String containsAllQuery = buildQuery(getSelectCountClause(), " where " + getReferenceName() + " in (:entities)"
				+ getAndClause(), NO_ORDER_SPECIFIED);
		logger.debug("containsAll query: " + containsAllQuery);
		return containsAllQuery;
	}

	private String getAllQuery() {
		String getAllQuery = buildQuery(getSelectClause(), getWhereClause(), getOrderClause());
		logger.debug("getAll query: " + getAllQuery);
		return getAllQuery;
	}

	private String getSizeQuery() {
		String sizeQuery = buildQuery(getSelectCountClause(), getWhereClause(), NO_ORDER_SPECIFIED);
		logger.debug("size query: " + sizeQuery);
		return sizeQuery;
	}

	private String getOrderClause() {
		return orderClause == null || orderClause.length() == 0 ? "" : " order by " + orderClause;
	}

	private String buildQuery(String operationClause, String whereClause, String orderClause) {
		return namePositionalParameters(operationClause + " " + getTablesClause() + whereClause + orderClause);
	}

	private String getEntityName() {
		return clazz.getSimpleName();
	}

	private String getJoinClause() {
		if (joinsClauses.isEmpty())
			return "";
		return " join " + StringUtils.collectionToDelimitedString(joinsClauses, " join ") + " ";
	}

	private String getWhereClause() {
		return enabledConditionsClauses().isEmpty() ? "" : " where " + getConditionsClause();
	}

	private List<JpaClause> enabledConditionsClauses() {
		List<JpaClause> enabled = new ArrayList<JpaClause>();
		for(JpaClause clause : conditionsClauses) {
			if (clause.isEnabled()) {
				enabled.add(clause);
			}
		}
		return enabled;
	}
	
	private String getConditionsClause() {

		List<String> clauses = new ArrayList<String>();

		for (JpaClause clause : enabledConditionsClauses()) {
				clauses.add(clause.getJpa());
		}

		return StringUtils.collectionToDelimitedString(clauses, " and ");

	}

	private String getAndClause() {
		return enabledConditionsClauses().isEmpty() ? "" : " and " + getConditionsClause();
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

	public boolean add(T entity) {
		if (isPersisted(entity)) {
			getJpaTemplate().merge(entity);
		} else {
			getJpaTemplate().persist(entity);
		}
		return false;
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

	private Map<String, Object> buildParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		for (JpaClause conditionClause : enabledConditionsClauses()) {
			params.putAll(conditionClause.getNamedParams());
			for (Object param : conditionClause.getPositionalParams()) {
				addUniqueParam(params, param);
			}
		}
		logger.debug("Using params: " + params);
		return params;
	}

	private void addUniqueParam(Map<String, Object> params, Object param) {
		String name = "param" + params.size();
		params.put(name, param);
	}

	private void addParamsTo(Query query) {
		for (Map.Entry<String, Object> entry : buildParams().entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Date) {
				query.setParameter(entry.getKey(), (Date) value, TemporalType.TIMESTAMP);
			} else if (value instanceof Calendar) {
				query.setParameter(entry.getKey(), (Calendar) value, TemporalType.TIMESTAMP);
			} else if (value instanceof DynaParam) {
				query.setParameter(entry.getKey(), ((DynaParam) value).getValue());
			} else {
				query.setParameter(entry.getKey(), value);
			}
		}
	}
	
	private void addPagingTo(Query query, int maxResults) {
		if (isPaged()) {
			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(maxResults);
		}
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
		int total = total();
		return total > pageSize ? pageSize : total;
	}
	
	public int total() {
		return ((Long)getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(getSizeQuery());
				addParamsTo(query);
				return query.getSingleResult();
			}

		})).intValue();
	}

	private String namePositionalParameters(String query) {
		int index = 0;
		while (query.contains("?")) {
			query = query.replaceFirst("\\?", ":param" + index++);
		}
		return query;
	}

	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	public Object[] toArray() {
		return getAll().toArray();
	}

	public T first() {
		Collection<T> all = getAll(1);
		return all.isEmpty() ? null : all.iterator().next();
	}

	private List<T> getAll() {
		return getAll(pageSize);
	}

	@SuppressWarnings("unchecked")
	private List<T> getAll(final int maxResults) {
		return getJpaTemplate().executeFind(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(getAllQuery());
				addParamsTo(query);
				addPagingTo(query, maxResults);

				return query.getResultList();
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

		getJpaTemplate().remove(entity);

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
				Query query = em.createQuery(getRetainAllQuery());
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
	public <E extends JpaActiveSet<T>> E where(JpaClause clause) {
		Assert.notNull(clause, "Clause was null");
		Assert.notNull(this.conditionsClauses, "Conditions clauses are null");
		
		List<JpaClause> combinedConditions = new ArrayList<JpaClause>(this.conditionsClauses);
		combinedConditions.add(clause);

		JpaActiveSet<T> copy = copy();
		copy.conditionsClauses = combinedConditions;

		return (E) copy;

	}
	
	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E ignoring(JpaClause ignoredClause) {
		Assert.notNull( ignoredClause, "Clause to ignore was null" );
		
		JpaActiveSet<T> copy = copy();
		if ( copy.conditionsClauses.remove(ignoredClause) == false) {
			throw new IllegalArgumentException("This clause was not found: " + ignoredClause + " Actual clauses: " + conditionsClauses);
		}
		return (E)copy;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E join(String join) {

		Assert.notNull(join, "Join was null");
		
		JpaActiveSet<T> copy = copy();
		copy.joinsClauses = new ArrayList<String>(this.joinsClauses);
		copy.joinsClauses.add(join);

		return (E) copy;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E orderedBy(String orderClause) {
		
		Assert.notNull(orderClause, "Order clause was null");
		
		JpaActiveSet<T> copy = copy();
		copy.orderClause = orderClause;
		return (E) copy;
	}

	public T find(Long id) {

		T finding = findOrNull(id);
		if (finding == null)
			throw new IllegalArgumentException("No " + getReferenceName() + " with id " + id);

		return finding;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E find(List<Long> ids) {
		return (E) where(getReferenceName() + "." + getIdReferenceName() + " in (?)", ids);
	}

	public T findOrNull(Long id) {
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

	protected String getReferenceName() {
		return clazz.getSimpleName().toLowerCase();
	}

	protected String getIdReferenceName() {
		return getIdField(clazz).getName();
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E from(String from) {
		JpaActiveSet<T> copy = copy();
		copy.fromClause = from;
		return (E) copy;
	}

	@SuppressWarnings("unchecked")
	public <E extends JpaActiveSet<T>> E select(String select) {
		JpaActiveSet<T> copy = copy();
		copy.selectClause = select;
		return (E) copy;
	}

}