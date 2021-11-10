/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class FilterCollector implements SearchLaunchesCollector {

	private LaunchRepository launchRepository;

	private UserFilterRepository userFilterRepository;

	@Autowired
	public FilterCollector(LaunchRepository launchRepository, UserFilterRepository userFilterRepository) {
		this.launchRepository = launchRepository;
		this.userFilterRepository = userFilterRepository;
	}

	@Override
	public List<Long> collect(Long filerId, Launch launch) {
		UserFilter userFilter = userFilterRepository.findByIdAndProjectId(filerId, launch.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT, filerId, launch.getProjectId()));
		ObjectType targetClass = userFilter.getTargetClass();
		expect(targetClass, equalTo(ObjectType.Launch)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Filter type '{}' is not supported", targetClass)
		);

		Filter filter = ProjectFilter.of(
				new Filter(targetClass.getClassObject(), Lists.newArrayList(userFilter.getFilterCondition())),
				launch.getProjectId());
		PageRequest pageable = PageRequest.of(0, LAUNCHES_FILTER_LIMIT, Sort.by(DESC, CRITERIA_START_TIME));
		return launchRepository.findByFilter(filter, pageable).stream().map(Launch::getId).collect(Collectors.toList());
	}
}
