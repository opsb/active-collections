package com.googlecode.activecollections.util;

import org.apache.commons.beanutils.PropertyUtils;

public class PropertyUtil {
	
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(Object root, String property) {
		try {
			return (T)PropertyUtils.getNestedProperty(root, property);
		} catch (Exception e) {
			throw new RuntimeException("Unable to get property <" + property + "> from " + root.getClass(), e);
		}
	}
	
}
