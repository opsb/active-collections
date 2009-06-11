package com.googlecode.activecollections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.log4j.Logger;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Transactional(propagation=Propagation.REQUIRED)
public class JpaActiveSet<T> extends ActiveSet<T> {

	private final Logger logger = Logger.getLogger(JpaActiveSet.class);
	
	private static final Integer DEFAULT_PAGE_SIZE = 25;

	private Field idField;
	
	private Class<T> clazz;

	private Integer page;
	
	private String conditionsClause = "";
	
	private Map<String,Object> params = new HashMap<String, Object>();
	
	private String orderClause;
	
	protected EntityManagerFactory entityManagerFactory;
	
	private JpaDaoSupport jpaDaoSupport;

	private Integer pageSize = DEFAULT_PAGE_SIZE;

	private String joinsClause;
	
	private String fromClause;
	
	private String selectClause;
	
	protected JpaActiveSet() {}
	
	public JpaActiveSet(Class<T> clazz, final EntityManagerFactory entityManagerFactory, String conditionsClause, String orderClause, List<Object> params) {
		
		Assert.notNull(entityManagerFactory, "Can not create a JpaActiveSet without an EntityManagerFactory, was given null");
		Assert.notNull(clazz, "Must specify a class");
		jpaDaoSupport = new JpaDaoSupport(){{
			setEntityManagerFactory(entityManagerFactory);			
		}};
		this.entityManagerFactory = entityManagerFactory;
		
		this.clazz = clazz;

		if (StringUtils.hasText(conditionsClause)) {
			JpaClause conditionsJpaClause = buildClause(conditionsClause, params);
			this.conditionsClause = conditionsJpaClause.getJpa();
			this.params = conditionsJpaClause.getParams();
		}
		
		this.orderClause = orderClause;
		this.idField = getIdField(clazz);
	}

	public JpaActiveSet(Class<T> clazz, EntityManagerFactory entityManagerFactory) {
		this( clazz, entityManagerFactory, "", "", new ArrayList<Object>() );
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
	
	protected <E extends JpaActiveSet<T>> void afterCopy(@SuppressWarnings("unused") E copy) {}

	private <E extends JpaActiveSet<T>> void  addMeta(E copy) {
		
		copy.clazz = clazz;
		copy.fromClause = fromClause;
		copy.selectClause = selectClause;
		copy.conditionsClause = conditionsClause;
		copy.orderClause = orderClause;
		copy.joinsClause = joinsClause;
		copy.idField = getIdField(clazz);
		copy.params = params;
		copy.page = page;
		copy.pageSize = pageSize;
		
	}
	
	private String getFromClause() {
		return "from " + (fromClause == null ? getEntityName() + " " + getReferenceName() : fromClause);
	}
	
	private String getSelectClause() {
		return "select " + (selectClause == null ? getReferenceName() : selectClause );
	}
	
	private String getSelectCountClause() {
		return "select count(" + (selectClause == null ? getReferenceName() : selectClause ) + ")";
	}
	
	private String getRetainAllQuery() {
		String retainAllQuery = "delete " + getFromClause() + " " + getJoinClause() + " where " + getReferenceName() + " not in (:entities)" + getAndClause();
		logger.debug("retainAllQuery: " + retainAllQuery);
		return retainAllQuery;
	}
	
	private String getContainsAllQuery() {
		String containsAllQuery = getSelectCountClause() +" " +getFromClause() + " " + getJoinClause() + " where " + getReferenceName() + " in (:entities)" + getAndClause();
		logger.debug("ContainsAll query " + containsAllQuery);
		return containsAllQuery;
	}
	
	private String getAllQuery() {
		String getAllQuery  = getSelectClause() + " " + getFromClause() + " " + getJoinClause() + getWhereClause() + (orderClause.length() == 0 ? "" : " order by " + orderClause);
		logger.debug("GetAll query " + getAllQuery);
		return getAllQuery;
	}
	
	private String getAndClause() {
		return conditionsClause.length() == 0 ? "" : " and " + conditionsClause;
	}
	
	private String getEntityName() {
		return clazz.getSimpleName();
	}
	
	private String getJoinClause() {
		if (!StringUtils.hasText(joinsClause)) return "";
		return " " + joinsClause + " ";
	}
	
	private String getWhereClause() {
		return conditionsClause.length() == 0 ? "" : " where " + conditionsClause;
	}
	
	private String getSizeQuery() {
		String sizeQuery = getSelectCountClause() + " " + getFromClause() + " " + getJoinClause() + getWhereClause();
		logger.debug(sizeQuery);
		return sizeQuery;
	}
	
	protected JpaTemplate getJpaTemplate() {
		return jpaDaoSupport.getJpaTemplate();
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

	@Override
	public boolean addAll(Collection<? extends T> entities) {
		
		for(T entity : entities) {
			add(entity);
		}
		
		boolean hasAddedEntities = !entities.isEmpty(); 
		
		return hasAddedEntities; 
	}
	
	@Override
	public boolean addAll(T ... entities) {
		return addAll(Arrays.asList(entities));
	}

	public void clear() {
		removeAll(this);
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
			else if (value instanceof DynamicParam) {
				query.setParameter(entry.getKey(), ((DynamicParam) value).getParam());
			}
			else {
				query.setParameter(entry.getKey(), value);
			}
		}
	}

	public boolean containsAll(final Collection<? extends Object> entities) {
		
		if (entities == null || entities.isEmpty()) return false;
		
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

	@Override
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
				addPagingTo(query);
				
				return query.getResultList();
			}

			private void addPagingTo(Query query) {
				if (isPaged()) {
					query.setFirstResult((page -1) * pageSize);
					query.setMaxResults(maxResults);
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

	private JpaClause buildClause(String conditionsClause, List<Object> params) {
		Map<String,Object> namedParams = new HashMap<String, Object>();
		
		int uidCounter = this.params.size();
		for(Object param : params) {
			String uniqueName = "param" + uidCounter++;
			conditionsClause = conditionsClause.replaceFirst("\\?", ":" + uniqueName);
			namedParams.put(uniqueName, param);
		}
		
		return new JpaClause(conditionsClause, namedParams);
	}
	
	private JpaClause buildClause(String conditionsClause, Object ... params) {
		return buildClause(conditionsClause, Arrays.asList(params));
		
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends ActiveSet<T>> E where(String conditionsClause, Object ... params) {
		
		return (E) where(buildClause(conditionsClause, params));
		
	}
	
	@SuppressWarnings("unchecked")
	private <E extends ActiveSet<T>> E where(JpaClause clause) {
		
		Map<String,Object> allParams = new HashMap<String,Object>();
		allParams.putAll(this.params);
		allParams.putAll(clause.getParams());
		
		boolean hasExistingClause = StringUtils.hasText(this.conditionsClause);
		String combinedConditionsClause = hasExistingClause ? this.conditionsClause + " and " + clause.getJpa() : clause.getJpa();
		
		JpaActiveSet<T> copy = copy();
		copy.conditionsClause = combinedConditionsClause;
		copy.params = allParams;
		
		return (E)copy;
		
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <E extends ActiveSet<T>> E join(String join) {
		
		String joinClause = "join " + join; 
		JpaActiveSet<T> copy = copy();
		boolean hasExistingJoins = StringUtils.hasText(this.joinsClause);
		String combinedJoinsClause = hasExistingJoins ? this.joinsClause + "  " + joinClause : joinClause;
		copy.joinsClause = combinedJoinsClause;
		
		return (E)copy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends ActiveSet<T> > E orderedBy(String orderClause) {
		JpaActiveSet<T> copy = copy();
		copy.orderClause = orderClause;
		return (E)copy;
	}

	@Override
	public T find(Long id) {
		
		T finding = findOrNull(id);
		if (finding == null) throw new IllegalArgumentException("No " + getReferenceName() + " with id " + id);
		
		return finding;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends ActiveSet<T>> E find(List<Long> ids) {
		return (E)where(getReferenceName() + "." + getIdReferenceName() + " in (?)", ids);
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

	@Override
	@SuppressWarnings("unchecked")
	public <E extends ActiveSet<T>> E pagesOf(Integer pageSize) {
		if (pageSize == null) return (E)this;
		JpaActiveSet<T> copy = copy();
		copy.pageSize = pageSize;
		copy.page = page == null? 1 : page;
		return (E)copy;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends ActiveSet<T>> E page(Integer page) {
		if (page == null) return (E)this;
		if (page < 1) throw new IllegalArgumentException("Page numbers start at 1");
		JpaActiveSet<T> copy = copy();
		copy.page = page;
		return (E)copy;
	}
	
	@Override
	public String toString() {
		Iterator<T> iter = iterator();
		StringBuilder s = new StringBuilder();
		while(iter.hasNext()) {
			s.append(iter.next().toString());
			if (iter.hasNext()) {
				s.append(", ");
			}
		}
		
		
		return s.toString();
	}
	
	@SuppressWarnings("unchecked")
	protected <E extends ActiveSet<T>> E all() {
		return (E) this;
	}
	
	@SuppressWarnings("unchecked")
	protected  <E extends ActiveSet<T>> E none() {
		return (E)where("true = false");
	}
	
	@Override
	public Set<T> frozen() {
		return new LinkedHashSet<T>(this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends ActiveSet<T>> E in(Collection<T> entities) {
		if (entities == null || entities.isEmpty()) return (E)none();
		return (E)where(getReferenceName() + " in (?)", entities);
	}

	@Override
	public List<T> frozenList() {
		return new ArrayList<T>(this);
	}
	
	@Override
	public SortedSet<T> frozenSortedSet() {
		return new TreeSet<T>(this);
	}
	
	@Override
	public Set<T> frozenSet() {
		return new HashSet<T>(this);
	}
	
	@Override
	public Collection<T> refreshAll(Collection<T> staleEntities) {
		if(CollectionUtils.isEmpty(staleEntities)) return staleEntities;
		return where(getReferenceName() + " in (?)", staleEntities);
	}

	protected String getReferenceName() {
		return clazz.getSimpleName().toLowerCase();
	}
	
	protected String getIdReferenceName() {
		return getIdField(clazz).getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends ActiveSet<T>> E from(String from) {
		JpaActiveSet<T> copy = copy();
		copy.fromClause = from;
		return (E)copy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends ActiveSet<T>> E select(String select) {
		JpaActiveSet<T> copy = copy();
		copy.selectClause = select;
		return (E)copy;
	}
	
}