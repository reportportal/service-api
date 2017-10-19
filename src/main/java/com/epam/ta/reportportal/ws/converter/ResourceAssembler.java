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
package com.epam.ta.reportportal.ws.converter;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Andrei Varabyeu
 */
public abstract class ResourceAssembler<T, R> implements Function<T, R> {

	/**
	 * Converts all given entities into resources.
	 *
	 * @param entities must not be {@literal null}.
	 * @return
	 * @see #toResource(Object)
	 */
	public List<R> toResources(Iterable<? extends T> entities) {

		Preconditions.checkNotNull(entities);
		List<R> result = new ArrayList<>();

		for (T entity : entities) {
			result.add(toResource(entity));
		}

		return result;
	}

	@Override
	public R apply(T t) {
		return toResource(t);
	}

	/**
	 * Converts the given entity into an another one
	 *
	 * @param entity Entity to convert
	 * @return Converted entity
	 */
	abstract R toResource(T entity);

}
