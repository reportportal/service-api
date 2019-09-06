package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class LaunchNameCollector implements SearchLaunchesCollector {

	private final LaunchRepository launchRepository;

	public LaunchNameCollector(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Override
	public List<Long> collect(Long filerId, Launch launch) {
		Filter filter = Filter.builder()
				.withTarget(Launch.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_NAME, launch.getName()).build())
				.build();
		PageRequest pageRequest = PageRequest.of(0, LAUNCHES_FILTER_LIMIT, Sort.by(Sort.Direction.DESC, CRITERIA_START_TIME));

		return launchRepository.findByFilter(filter, pageRequest).stream().map(Launch::getId).collect(Collectors.toList());
	}
}
