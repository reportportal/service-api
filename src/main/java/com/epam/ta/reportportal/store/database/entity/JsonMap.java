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

package com.epam.ta.reportportal.store.database.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class JsonMap<K, V> extends LinkedHashMap<K, V> implements Serializable, Map<K, V> {

	public JsonMap() {
	}

	public JsonMap(Map<? extends K, ? extends V> m) {
		super(m);
	}

	public JsonMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}

	public JsonMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public JsonMap(int initialCapacity) {
		super(initialCapacity);
	}

	public static <K, V> JsonMap<K, V> ofMap(Map<K, V> map) {
		return new JsonMap<>(map);
	}
}
