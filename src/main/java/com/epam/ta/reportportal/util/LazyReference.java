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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Bean Reference. Added to simplify autowiring of prototype to singleton
 *
 * @param <T>
 * @author Andrei Varabyeu
 * @deprecated in favor of {@link javax.inject.Provider}
 */
@Deprecated
public class LazyReference<T> implements ApplicationContextAware {

	/*
	 * Strategy for finding beans
	 */
	private FindBeanStrategy<T> beanFindStrategy;

	/*
	 * Supplier. Holds information how to access bean
	 */
	private Supplier<T> supplier;

	public LazyReference(Class<T> clazz) {
		Preconditions.checkNotNull(clazz, "Bean class shouldn't be null");
		this.beanFindStrategy = new ByClassFindStrategy<>(clazz);
	}

	public LazyReference(String name) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Bean name shouldn't be null");
		this.beanFindStrategy = new ByNameFindStrategy<>(name);
	}

	@Override
	public void setApplicationContext(final ApplicationContext context) throws BeansException {
		supplier = createBeanSupplier(context);
	}

	public T get() {
		return supplier.get();
	}

	/**
	 * Creates bean supplier. Please, override this method in subclasses if
	 * Necessary
	 *
	 * @param context
	 * @return
	 */
	protected Supplier<T> createBeanSupplier(final ApplicationContext context) {
		return () -> getBeanFromContext(context);
	}

	private T getBeanFromContext(ApplicationContext context) {
		return beanFindStrategy.findBean(context);
	}

	/**
	 * Strategy for retrieving beans from Spring's context
	 *
	 * @param <T>
	 * @author Andrei Varabyeu
	 */
	protected interface FindBeanStrategy<T> {
		T findBean(ApplicationContext context);
	}

	/**
	 * Loads bean from Spring's context by bean name/ID
	 *
	 * @param <T>
	 * @author Andrei Varabyeu
	 */
	private static class ByNameFindStrategy<T> implements FindBeanStrategy<T> {

		private String name;

		public ByNameFindStrategy(String name) {
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T findBean(ApplicationContext context) {
			return (T) context.getBean(name);
		}

	}

	/**
	 * Loads bean from Spring's context by bean type
	 *
	 * @param <T>
	 * @author Andrei Varabyeu
	 */
	private static class ByClassFindStrategy<T> implements FindBeanStrategy<T> {

		private Class<T> clazz;

		public ByClassFindStrategy(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T findBean(ApplicationContext context) {
			return (T) context.getBean(clazz);
		}

	}

}