/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.filter.predefined;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.user.User;
import com.google.common.collect.ImmutableMap;
import org.jooq.Operator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ORGANIZATION;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.*;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TYPE;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.*;
import static com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType.*;

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
	private static final Collection<TestItemTypeEnum> HAS_METHOD_OR_CLASS = Arrays.stream(TestItemTypeEnum.values()).filter(it -> {
		String name = it.name();
		return name.contains("METHOD") || name.contains("CLASS");
	}).collect(Collectors.toList());

	//@formatter:off
	private static final Map<PredefinedFilterType, PredefinedFilterBuilder> FILTERS = ImmutableMap.<PredefinedFilterType, PredefinedFilterBuilder>builder()
			.put(COLLAPSED, new PredefinedFilterBuilder() {
				@Override
				public Queryable build(String[] params) {
					return Filter.builder()
						.withTarget(TestItem.class)
						.withCondition(new FilterCondition(Operator.OR, Condition.EQUALS, false, StatusEnum.FAILED.name(), CRITERIA_STATUS))
						.withCondition(new FilterCondition(Operator.OR, Condition.IN, true, HAS_METHOD_OR_CLASS.stream().map(Enum::name).collect(Collectors.joining(",")), CRITERIA_TYPE))
						.withCondition(new FilterCondition(Operator.OR, Condition.EXISTS, false, "true", CRITERIA_ISSUE_TYPE))
						.build();
				}
			})
			.put(USERS, new PredefinedFilterBuilder() {
				@Override
				public Queryable build(String[] params) {
					return Filter.builder()
								.withTarget(User.class)
								.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, params[0], CRITERIA_USER))
								.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, params[0], CRITERIA_FULL_NAME))
								.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, params[0], CRITERIA_EMAIL))
								.build();
				}
			})
			.put(PROJECTS, new PredefinedFilterBuilder() {
				@Override
				public Queryable build(String[] params) {
					return Filter.builder()
								.withTarget(ProjectInfo.class)
								.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, params[0], CRITERIA_PROJECT_NAME))
								.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, params[0], CRITERIA_PROJECT_ORGANIZATION))
								.build();
				}
			})
			.build();
	//@formatter:on

	public static boolean hasFilter(PredefinedFilterType type) {
		return FILTERS.containsKey(type);
	}

	public static Queryable buildFilter(PredefinedFilterType type, String[] params) {
		final PredefinedFilterBuilder builder = FILTERS.get(type);
		return builder.buildFilter(params);
	}

}
