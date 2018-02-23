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

import java.lang.reflect.Field;

/**
 * Setter and Getter for Accessible Field
 *
 * @author Andrei Varabyeu
 */
public class AccessibleField {

	private final Field f;
	private final Object bean;

	AccessibleField(Object bean, Field f) {
		this.bean = bean;
		this.f = f;
	}

	public Class<?> getType() {
		return this.f.getType();
	}

	public void setValue(Object value) {
		try {
			this.f.set(this.bean, value);
		} catch (IllegalAccessException accessException) { //NOSONAR
			this.f.setAccessible(true);
			try {
				this.f.set(this.bean, value);
			} catch (IllegalAccessException e) { //NOSONAR
				throw new IllegalAccessError(e.getMessage());
			}
		}
	}

	public Object getValue() {
		try {
			return this.f.get(this.bean);
		} catch (IllegalAccessException accessException) { //NOSONAR
			this.f.setAccessible(true);
			try {
				return this.f.get(this.bean);
			} catch (IllegalAccessException e) { //NOSONAR
				throw new IllegalAccessError(e.getMessage());
			}
		}
	}
}