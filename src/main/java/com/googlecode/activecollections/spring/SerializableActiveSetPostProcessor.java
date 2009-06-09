package com.googlecode.activecollections.spring;


import org.apache.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.googlecode.activecollections.ActiveSet;
import com.googlecode.activecollections.JpaActiveSet;

public class SerializableActiveSetPostProcessor implements BeanPostProcessor, DisposableBean {

	private final Logger logger = Logger.getLogger(JpaActiveSet.class);
	static ApplicationContext applicationContext;
	
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {

		if (!(bean instanceof ActiveSet)) return bean; 
		
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("enhancing bean: " + beanName + " " + bean.getClass());
			}
			
			ProxyFactory factory = new ProxyFactory(bean);
			factory.setProxyTargetClass(true);
			factory.addAdvice(new SerializableBeanAdvice(beanName));
			return factory.getProxy();
		}
		catch(Exception e) {
			logger.error("Unable to add serializable proxy to " + beanName);
			e.printStackTrace();
			return bean;
		}
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SerializableActiveSetPostProcessor.applicationContext = applicationContext;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		Assert.notNull(applicationContext, "No application context available, can not get bean: " + name);
		return (T)applicationContext.getBean(name);
	}

	public void destroy() throws Exception {
		applicationContext = null;
	}
	
}
