/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.commons.accessible;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Accessible method implementation. Set accessibility == true for specified
 * method and can invoke methods
 *
 * @author Andrei Varabyeu
 */
public class AccessibleMethod {

	private final Method method;
	private final Object bean;

	AccessibleMethod(Object bean, Method method) {
		this.bean = bean;
		this.method = method;
	}

	public Object invoke(Object... args) throws Throwable {
		try {
			return invoke(this.bean, this.method, args);
		} catch (IllegalAccessException accessException) { //NOSONAR
			this.method.setAccessible(true);
			try {
				return invoke(this.bean, this.method, args);
			} catch (IllegalAccessException e) { //NOSONAR
				throw new IllegalAccessError(e.getMessage());
			}
		}

	}

	private Object invoke(Object bean, Method m, Object... args) throws Throwable {
		try {
			return m.invoke(bean, args);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}

	}

}
