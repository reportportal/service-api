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
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static java.util.stream.Collectors.toList;

@Service
public class StepBasedDemoDataFacade extends DemoDataCommonService implements DemoDataFacade {

	private static final StatisticsCalculationStrategy strategy = StatisticsCalculationStrategy.STEP_BASED;

	@Value("classpath:demo/demo_data.json")
	private Resource resource;

	@Override
	public List<String> generateDemoLaunches(DemoDataRq demoDataRq, String user, String projectName) {
		Map<String, Map<String, List<String>>> suites;
		try {
			suites = objectMapper.readValue(resource.getURL(), new TypeReference<Map<String, Map<String, List<String>>>>() {
			});
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load suites description. " + e.getMessage(), e);
		}
		return generateLaunches(demoDataRq, suites, user, projectName, strategy);
	}

	private List<String> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> suitesStructure, String user,
			String project, StatisticsCalculationStrategy statsStrategy) {
		return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
			String launchId = startLaunch(NAME + "_" + rq.getPostfix(), i, project, user);
			generateSuites(suitesStructure, project, i, launchId, statsStrategy);
			finishLaunch(launchId);
			return launchId;
		}).collect(toList());
	}

	private List<String> generateSuites(Map<String, Map<String, List<String>>> suitesStructure, String project, int i, String launchId,
			StatisticsCalculationStrategy statsStrategy) {
		return suitesStructure.entrySet().stream().limit(i + 1).map(suites -> {
			TestItem suiteItem = startRootItem(suites.getKey(), launchId, SUITE, project);
			suites.getValue().entrySet().forEach(tests -> {
				TestItem testItem = startTestItem(suiteItem, launchId, tests.getKey(), TEST, project);
				String beforeClassStatus = "";
				boolean isGenerateClass = ContentUtils.getWithProbability(STORY_PROBABILITY);
				if (isGenerateClass) {
					TestItem beforeClass = startTestItem(testItem, launchId, "beforeClass", BEFORE_CLASS, project);
					beforeClassStatus = status();
					finishTestItem(beforeClass.getId(), beforeClassStatus, statsStrategy);
				}
				boolean isGenerateBeforeMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				boolean isGenerateAfterMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				tests.getValue().stream().limit(i + 1).forEach(name -> {
					if (isGenerateBeforeMethod) {
						finishTestItem(startTestItem(testItem, launchId, "beforeMethod", BEFORE_METHOD, project).getId(), status(),
								statsStrategy
						);
					}
					TestItem stepId = startTestItem(testItem, launchId, name, STEP, project);
					String status = status();
					logDemoDataService.generateDemoLogs(stepId.getId(), status, project);
					finishTestItem(stepId.getId(), status, statsStrategy);
					if (isGenerateAfterMethod) {
						finishTestItem(
								startTestItem(testItem, launchId, "afterMethod", AFTER_METHOD, project).getId(), status(), statsStrategy);
					}
				});
				if (isGenerateClass) {
					TestItem afterClass = startTestItem(testItem, launchId, "afterClass", AFTER_CLASS, project);
					finishTestItem(afterClass.getId(), status(), statsStrategy);
				}
				finishTestItem(testItem.getId(), !beforeClassStatus.isEmpty() ? beforeClassStatus : "FAILED", statsStrategy);
			});
			finishRootItem(suiteItem.getId());
			return suiteItem.getId();
		}).collect(toList());
	}
}
