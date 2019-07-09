/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
