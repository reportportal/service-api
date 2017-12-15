/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * {@link FactoryBean} with access to {@link ApplicationContext} with lazy
 * initialization
 *
 * @param <T> - type of bean
 * @author Andrei Varabyeu
 */
public abstract class ApplicationContextAwareFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, InitializingBean {

	/**
	 * Application context holder
	 */
	private ApplicationContext applicationContext;

	/**
	 * Supplier of bean to be created
	 */
	private Supplier<T> beanSupplier;

	/**
	 * Whether is bean to be creates going to be singleton
	 */
	private boolean singleton = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public T getObject() {
		return beanSupplier.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * Instantiates supplier for bean to be created. This mades possible
	 * lazy-initialization
	 */
	@Override
	public void afterPropertiesSet() {
		Supplier<T> supplier = this::createInstance;

		this.beanSupplier = isSingleton() ? Suppliers.memoize(supplier) : supplier;
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Template method that subclasses must override to construct the object
	 * returned by this factory.
	 * <p>
	 * Invoked on initialization of this FactoryBean in case of a singleton;
	 * else, on each {@link #getObject()} call.
	 *
	 * @return the object returned by this factory
	 * @see #getObject()
	 */
	protected abstract T createInstance();
}
