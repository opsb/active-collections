package com.googlecode.activecollections.spring;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;

public class SerializableBeanAdvice implements MethodBeforeAdvice, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String beanName;

	SerializableBeanAdvice(String beanName) {
		this.beanName = beanName;
	}

	public void before(Method method, Object[] args, Object target) throws Throwable {
		if (target == null) target = SerializableActiveSetPostProcessor.getBean(beanName);
		method.invoke(target, args);
	}
	
}