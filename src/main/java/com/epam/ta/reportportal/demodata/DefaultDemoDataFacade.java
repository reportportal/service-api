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

package com.epam.ta.reportportal.demodata;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Ihar Kahadouski
 */
@Service
public class DefaultDemoDataFacade extends DemoDataCommonService implements DemoDataFacade {

	@Value("classpath:demo/demo_data.json")
	private Resource resource;

	@Override
	public List<Long> generateDemoLaunches(DemoDataRq demoDataRq, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		Map<String, Map<String, List<String>>> suites;
		try {
			suites = objectMapper.readValue(resource.getURL(), new TypeReference<Map<String, Map<String, List<String>>>>() {
			});
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load suites description. " + e.getMessage(), e);
		}
		return generateLaunches(demoDataRq, suites, user, projectDetails);
	}

	private List<Long> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> suitesStructure, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
			Long launchId = startLaunch(NAME, i, user, projectDetails);
			generateSuites(suitesStructure, i, launchId, user, projectDetails);
			finishLaunch(launchId, user, projectDetails);
			return launchId;
		}).collect(toList());
	}

	private List<Long> generateSuites(Map<String, Map<String, List<String>>> suitesStructure, int i, Long launchId, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		return suitesStructure.entrySet().stream().limit(i + 1).map(suites -> {
			Long suiteItemId = startRootItem(suites.getKey(), launchId, SUITE, user, projectDetails);
			suites.getValue().entrySet().forEach(tests -> {
				Long testItemId = startTestItem(suiteItemId, launchId, tests.getKey(), TEST, user, projectDetails);
				String beforeClassStatus = "";
				boolean isGenerateClass = ContentUtils.getWithProbability(STORY_PROBABILITY);
				if (isGenerateClass) {
					Long beforeClassId = startTestItem(testItemId, launchId, "beforeClass", BEFORE_CLASS, user, projectDetails);
					beforeClassStatus = status();
					finishTestItem(beforeClassId, beforeClassStatus, user, projectDetails);
				}
				boolean isGenerateBeforeMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				boolean isGenerateAfterMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				tests.getValue().stream().limit(i + 1).forEach(name -> {
					if (isGenerateBeforeMethod) {
						finishTestItem(
								startTestItem(testItemId, launchId, "beforeMethod", BEFORE_METHOD, user, projectDetails),
								status(),
								user,
								projectDetails
						);
					}
					Long stepId = startTestItem(testItemId, launchId, name, STEP, user, projectDetails);
					String status = status();
					logDemoDataService.generateDemoLogs(testItemRepository.findById(stepId).get(), status);
					finishTestItem(stepId, status, user, projectDetails);
					if (isGenerateAfterMethod) {
						finishTestItem(
								startTestItem(testItemId, launchId, "afterMethod", AFTER_METHOD, user, projectDetails),
								status(),
								user,
								projectDetails
						);
					}
				});
				if (isGenerateClass) {
					Long afterClassId = startTestItem(testItemId, launchId, "afterClass", AFTER_CLASS, user, projectDetails);
					finishTestItem(afterClassId, status(), user, projectDetails);
				}
				finishTestItem(testItemId, !beforeClassStatus.isEmpty() ? beforeClassStatus : "FAILED", user, projectDetails);
			});
			finishRootItem(suiteItemId, user, projectDetails);
			return suiteItemId;
		}).collect(toList());
	}
}
