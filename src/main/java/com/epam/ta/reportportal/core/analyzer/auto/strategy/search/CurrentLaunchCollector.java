package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class CurrentLaunchCollector implements SearchLaunchesCollector {

	@Override
	public List<Long> collect(Long filerId, Launch launch) {
		return Collections.singletonList(launch.getId());
	}
}
