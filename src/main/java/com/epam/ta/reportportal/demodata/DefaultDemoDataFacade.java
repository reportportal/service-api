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
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.demodata.Constants.NAME;
import static com.epam.ta.reportportal.demodata.Constants.STORY_PROBABILITY;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Ihar Kahadouski
 */
@Service
public class DefaultDemoDataFacade implements DemoDataFacade {

	private final DemoDataCommonService demoDataCommonService;

	private final DemoLogsService demoLogsService;

	private final TestItemRepository testItemRepository;

	private final ObjectMapper objectMapper;

	@Value("classpath:demo/demo_data.json")
	private Resource resource;

	@Autowired
	public DefaultDemoDataFacade(DemoDataCommonService demoDataCommonService, DemoLogsService demoLogsService,
			TestItemRepository testItemRepository, ObjectMapper objectMapper) {
		this.demoDataCommonService = demoDataCommonService;
		this.demoLogsService = demoLogsService;
		this.testItemRepository = testItemRepository;
		this.objectMapper = objectMapper;
	}

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
			Long launchId = demoDataCommonService.startLaunch(NAME, i, user, projectDetails);
			generateSuites(suitesStructure, i, launchId, user, projectDetails);
			demoDataCommonService.finishLaunch(launchId, user, projectDetails);
			return launchId;
		}).collect(toList());
	}

	private List<Long> generateSuites(Map<String, Map<String, List<String>>> suitesStructure, int i, Long launchId, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		return suitesStructure.entrySet().stream().limit(i + 1).map(suites -> {
			Long suiteItemId = demoDataCommonService.startRootItem(suites.getKey(), launchId, SUITE, user, projectDetails);
			suites.getValue().entrySet().forEach(tests -> {
				Long testItemId = demoDataCommonService.startTestItem(suiteItemId, launchId, tests.getKey(), TEST, user, projectDetails);
				String beforeClassStatus = "";
				boolean isGenerateClass = ContentUtils.getWithProbability(STORY_PROBABILITY);
				if (isGenerateClass) {
					Long beforeClassId = demoDataCommonService.startTestItem(
							testItemId,
							launchId,
							"beforeClass",
							BEFORE_CLASS,
							user,
							projectDetails
					);
					beforeClassStatus = status();
					demoDataCommonService.finishTestItem(beforeClassId, beforeClassStatus, user, projectDetails);
				}
				boolean isGenerateBeforeMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				boolean isGenerateAfterMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				tests.getValue().stream().limit(i + 1).forEach(name -> {
					if (isGenerateBeforeMethod) {
						demoDataCommonService.finishTestItem(
								demoDataCommonService.startTestItem(
										testItemId,
										launchId,
										"beforeMethod",
										BEFORE_METHOD,
										user,
										projectDetails
								),
								status(),
								user,
								projectDetails
						);
					}
					Long stepId = demoDataCommonService.startTestItem(testItemId, launchId, name, STEP, user, projectDetails);
					String status = status();
					demoLogsService.generateDemoLogs(testItemRepository.findById(stepId).get(), status);
					demoDataCommonService.finishTestItem(stepId, status, user, projectDetails);
					if (isGenerateAfterMethod) {
						demoDataCommonService.finishTestItem(
								demoDataCommonService.startTestItem(
										testItemId,
										launchId,
										"afterMethod",
										AFTER_METHOD,
										user,
										projectDetails
								),
								status(),
								user,
								projectDetails
						);
					}
				});
				if (isGenerateClass) {
					Long afterClassId = demoDataCommonService.startTestItem(
							testItemId,
							launchId,
							"afterClass",
							AFTER_CLASS,
							user,
							projectDetails
					);
					demoDataCommonService.finishTestItem(afterClassId, status(), user, projectDetails);
				}
				demoDataCommonService.finishTestItem(
						testItemId,
						!beforeClassStatus.isEmpty() ? beforeClassStatus : "FAILED",
						user,
						projectDetails
				);
			});
			demoDataCommonService.finishRootItem(suiteItemId, user, projectDetails);
			return suiteItemId;
		}).collect(toList());
	}

	String status() {
		int STATUS_PROBABILITY = 15;
		if (ContentUtils.getWithProbability(STATUS_PROBABILITY)) {
			return SKIPPED.name();
		} else if (ContentUtils.getWithProbability(2 * STATUS_PROBABILITY)) {
			return FAILED.name();
		}
		return PASSED.name();
	}
}
