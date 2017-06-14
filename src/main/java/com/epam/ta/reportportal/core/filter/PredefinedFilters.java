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
package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.search.PredefinedFilter;
import com.epam.ta.reportportal.database.search.Queryable;
import com.epam.ta.reportportal.ws.resolver.PredefinedFilterBuilder;
import com.google.common.collect.ImmutableMap;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Holder for predefined quires
 *
 * @author Andrei Varabyeu
 */
public final class PredefinedFilters {

	private PredefinedFilters() {
		//no instance required
	}

	/**
	 * Костыль requested by UI team. Back-end team doesn't really understand what such a strange
	 * query is supposed to be used for.
	 * TODO Incompatible with free structure tree and BDD-like structure
	 */
	private static final Collection<TestItemType> HAS_METHOD_OR_CLASS = Arrays.stream(TestItemType.values()).filter(it -> {
		String name = it.name();
		return name.contains("METHOD") || name.contains("CLASS");
	}).collect(Collectors.toList());

	//@formatter:off
	private static final Map<String, PredefinedFilterBuilder> FILTERS = ImmutableMap.<String, PredefinedFilterBuilder>builder()
			.put("collapsed", new PredefinedFilterBuilder() {
				@Override
				public Queryable build(String[] params) {
					return new PredefinedFilter(TestItem.class, singletonList(
							new Criteria()
									.orOperator(
											where("status").is(Status.FAILED),
											where("type").nin(HAS_METHOD_OR_CLASS),
											where("issue").exists(true))));
				}
			}).build();
	//@formatter:on

	public static boolean hasFilter(String name) {
		return FILTERS.containsKey(name);
	}

	public static Queryable buildFilter(String name, String[] params) {
		final PredefinedFilterBuilder builder = FILTERS.get(name);
		return builder.buildFilter(params);
	}

}
