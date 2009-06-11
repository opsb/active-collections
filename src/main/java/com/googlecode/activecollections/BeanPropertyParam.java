package com.googlecode.activecollections;

import com.googlecode.activecollections.util.PropertyUtil;

public class BeanPropertyParam implements DynamicParam {

	private Object bean;
	
	private String property;
	
	public BeanPropertyParam(Object bean, String property) {
		this.bean = bean;
		this.property = property;
	}
	
	public Object getParam() {
		return PropertyUtil.getProperty(bean, property);
	}
	
	public static Object beanParam(Object bean, String property) {
		return new BeanPropertyParam(bean, property);
	}

}
