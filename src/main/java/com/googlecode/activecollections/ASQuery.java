package com.googlecode.activecollections;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class ASQuery {

	private static final String NO_ORDER_SPECIFIED = "";
	
	private List<JpaClause> conditionsClauses = new ArrayList<JpaClause>();

	private List<String> orderClauses = new ArrayList<String>();

	private List<String> joinsClauses = new ArrayList<String>();

	private String fromClause;

	private String selectClause;

	private boolean distinct = false;

	private Logger logger;
	
	private String referenceName;
	
	private String entityName;
	
	public ASQuery(Logger logger, String referenceName, String entityName) {
		this.logger = logger;
		this.referenceName = referenceName;
		this.entityName = entityName;
	}
	
	public ASQuery copy() {
		ASQuery copy = new ASQuery(logger, referenceName, entityName);
		copy.fromClause = fromClause;
		copy.selectClause = selectClause;
		copy.distinct = distinct;
		copy.conditionsClauses = new ArrayList<JpaClause>(this.conditionsClauses);
		copy.orderClauses = new ArrayList<String>(orderClauses);
		copy.joinsClauses = new ArrayList<String>(joinsClauses);
		copy.entityName = entityName;
		copy.referenceName = referenceName;
		return copy;
	}
	
	private String getReferenceName() {
		return referenceName;
	}
	
	private String getEntityName() {
		return entityName;
	}
	
	private Logger getLogger() {
		return logger;
	}

	private String getFromClause() {
		return "from " + (fromClause == null ? getEntityName() + " " + getReferenceName() : fromClause);
	}

	private String getSelectFragmentWithOptionsApplied() {
		String selectFragment = this.selectClause;
		if (selectFragment == null) selectFragment = getReferenceName();
		if (distinct) selectFragment = "distinct " + selectFragment;
		return selectFragment;
	}
	
	private String getSelectClause() {
		return "select " + getSelectFragmentWithOptionsApplied();
	}

	private String getSelectCountClause() {
		return "select count(" + getSelectFragmentWithOptionsApplied() + ")";
	}

	private String getTablesClause() {
		return getFromClause() + getJoinClause();
	}

	private String getDeleteClause() {
		return "delete";
	}

	public String getRetainAllQuery() {
		String retainAllQuery = buildQuery(getDeleteClause(), " where " + getReferenceName() + " not in (:entities)"
				+ getAndClause(), NO_ORDER_SPECIFIED);
		getLogger().debug("retainAll query: " + retainAllQuery);
		return retainAllQuery;
	}

	public String getContainsAllQuery() {
		String containsAllQuery = buildQuery(getSelectCountClause(), " where " + getReferenceName() + " in (:entities)"
				+ getAndClause(), NO_ORDER_SPECIFIED);
		getLogger().debug("containsAll query: " + containsAllQuery);
		return containsAllQuery;
	}

	public String getAllQuery() {
		String getAllQuery = buildQuery(getSelectClause(), getWhereClause(), getOrderClause());
		getLogger().debug("getAll query: " + getAllQuery);
		return getAllQuery;
	}
	
	private String namePositionalParameters(String query) {
		int index = 0;
		while (query.contains("?")) {
			query = query.replaceFirst("\\?", ":param" + index++);
		}
		return query;
	}	
	
	private String getJoinClause() {
		if (joinsClauses.isEmpty())
			return "";
		return " " + StringUtils.collectionToDelimitedString(joinsClauses, " ");
	}
	
	private String buildQuery(String operationClause, String whereClause, String orderClause) {
		return namePositionalParameters(operationClause + " " + getTablesClause() + whereClause + orderClause);
	}

	public String getSizeQuery() {
		String sizeQuery = buildQuery(getSelectCountClause(), getWhereClause(), NO_ORDER_SPECIFIED);
		getLogger().debug("size query: " + sizeQuery);
		return sizeQuery;
	}
	
	private String getAndClause() {
		return enabledConditionsClauses().isEmpty() ? "" : " and " + getConditionsClause();
	}

	private String getOrderClause() {
		if(orderClauses == null || orderClauses.size() <= 0) return "";
		
		StringBuilder orderClause = new StringBuilder(" order by ");
		orderClause.append( StringUtils.collectionToCommaDelimitedString(orderClauses) );
		
		return orderClause.toString();
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
	
	private Map<String, Object> buildParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		for (JpaClause conditionClause : enabledConditionsClauses()) {
			params.putAll(conditionClause.getNamedParams());
			for (Object param : conditionClause.getPositionalParams()) {
				addUniqueParam(params, param);
			}
		}
		getLogger().debug("Using params: " + params);
		return params;
	}

	private void addUniqueParam(Map<String, Object> params, Object param) {
		String name = "param" + params.size();
		params.put(name, param);
	}

	void addParamsTo(Query query) {
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

	public void add(JpaClause clause) {
		conditionsClauses.add(clause);
	}

	public void merge(ASQuery asQuery) {
		joinsClauses.addAll(asQuery.joinsClauses);
		conditionsClauses.addAll(asQuery.conditionsClauses);
	}

	public boolean remove(JpaClause ignoredClause) {
		 return conditionsClauses.remove(ignoredClause);
	}

	public void addOrderClause(String orderClause) {
		orderClauses.add(orderClause);
	}

	public void distinct() {
		this.distinct = true;
	}

	public void select(String select) {
		selectClause = select;
	}

	public void from(String from) {
		fromClause = from;
	}

	public void join(String joinType, String joinClause) {
		joinsClauses.add(joinType + " " + joinClause);
	}
	
}
