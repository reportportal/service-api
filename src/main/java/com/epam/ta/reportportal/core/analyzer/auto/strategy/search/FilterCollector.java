package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.epam.ta.reportportal.commons.querygen.Filter;
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
		expect(userFilter.getTargetClass(), equalTo(ObjectType.Launch)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Filter type '{}' is not supported", userFilter.getTargetClass())
		);

		Filter filter = new Filter(userFilter.getTargetClass().getClassObject(), Lists.newArrayList(userFilter.getFilterCondition()));
		PageRequest pageable = PageRequest.of(0, LAUNCHES_FILTER_LIMIT, Sort.by(Sort.Direction.DESC, CRITERIA_START_TIME));
		return launchRepository.findByFilter(filter, pageable).stream().map(Launch::getId).collect(Collectors.toList());
	}
}
