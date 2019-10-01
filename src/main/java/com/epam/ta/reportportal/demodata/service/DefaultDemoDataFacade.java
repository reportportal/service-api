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

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.demodata.model.DemoDataRq;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.demodata.service.Constants.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Ihar Kahadouski
 */
@Service
public class DefaultDemoDataFacade implements DemoDataFacade {

	private final DemoDataLaunchService demoDataLaunchService;

	private final DemoDataTestItemService demoDataTestItemService;

	private final DemoLogsService demoLogsService;

	private final ObjectMapper objectMapper;

	private final UserRepository userRepository;

	private final TaskExecutor executor;

	@Value("classpath:demo/demo_data.json")
	private Resource resource;

	public DefaultDemoDataFacade(DemoDataLaunchService demoDataLaunchService, DemoDataTestItemService demoDataTestItemService,
			DemoLogsService demoLogsService, ObjectMapper objectMapper, UserRepository userRepository,
			@Qualifier("demoDataTaskExecutor") TaskExecutor executor) {
		this.demoDataLaunchService = demoDataLaunchService;
		this.demoDataTestItemService = demoDataTestItemService;
		this.demoLogsService = demoLogsService;
		this.objectMapper = objectMapper;
		this.userRepository = userRepository;
		this.executor = executor;
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

		User creator = userRepository.findById(user.getUserId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUsername()));

		List<CompletableFuture<Long>> futures = IntStream.range(0, rq.getLaunchesQuantity())
				.mapToObj(i -> CompletableFuture.supplyAsync(() -> {
					Launch launch = demoDataLaunchService.startLaunch(NAME, i, creator, projectDetails);
					generateSuites(suitesStructure, i, launch.getUuid(), user, projectDetails);
					demoDataLaunchService.finishLaunch(launch.getUuid());
					return launch.getId();
				}, executor))
				.collect(toList());
		return futures.stream().map(CompletableFuture::join).collect(toList());
	}

	private void generateSuites(Map<String, Map<String, List<String>>> suitesStructure, int i, String launchId, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		suitesStructure.entrySet().stream().limit(i + 1).forEach(suites -> {
			String suiteItemId = demoDataTestItemService.startRootItem(suites.getKey(), launchId, SUITE, user, projectDetails);
			suites.getValue().forEach((key, value) -> {
				String testItemId = demoDataTestItemService.startTestItem(suiteItemId, launchId, key, TEST, false, user, projectDetails);
				Optional<StatusEnum> beforeClassStatus = Optional.empty();
				boolean isGenerateClass = ContentUtils.getWithProbability(STORY_PROBABILITY);
				if (isGenerateClass) {
					beforeClassStatus = Optional.of(status());
					generateStepItem(testItemId, launchId, user, projectDetails, BEFORE_CLASS, beforeClassStatus.get());
				}
				boolean isGenerateBeforeMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				boolean isGenerateAfterMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				value.stream().limit(i + 1).forEach(name -> {
					if (isGenerateBeforeMethod) {
						generateStepItem(testItemId, launchId, user, projectDetails, BEFORE_METHOD, status());
					}

					StatusEnum status = status();
					generateStepWithLogs(launchId, testItemId, name, false, projectDetails, user, status);
					generateRetries(launchId, testItemId, name, user, projectDetails, status);

					if (isGenerateAfterMethod) {
						generateStepItem(testItemId, launchId, user, projectDetails, AFTER_METHOD, status());
					}
				});
				if (isGenerateClass) {
					generateStepItem(testItemId, launchId, user, projectDetails, AFTER_CLASS, status());
				}
				demoDataTestItemService.finishTestItem(testItemId, beforeClassStatus.orElse(StatusEnum.FAILED), user, projectDetails);
			});
			demoDataTestItemService.finishRootItem(suiteItemId, user, projectDetails);
		});
	}

	private void generateRetries(String launchId, String testItemId, String name, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails, StatusEnum status) {
		if (status != PASSED && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			while ((status = status()) != PASSED) {
				generateStepWithLogs(launchId, testItemId, name, true, projectDetails, user, status);
			}
		}
	}

	private void generateStepWithLogs(String launchId, String rootItemId, String name, boolean retry,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, StatusEnum status) {
		String stepId = demoDataTestItemService.startTestItem(rootItemId, launchId, name, STEP, retry, user, projectDetails);
		List<Log> logs = demoLogsService.generateDemoLogs(stepId, status, projectDetails.getProjectId(), launchId);
		demoLogsService.attachFiles(logs, projectDetails.getProjectId(), stepId, launchId);
		demoDataTestItemService.finishTestItem(stepId, status, user, projectDetails);
	}

	private void generateStepItem(String parentId, String launchId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails,
			TestItemTypeEnum type, StatusEnum status) {

		String beforeMethodId = demoDataTestItemService.startTestItem(parentId,
				launchId,
				type.name().toLowerCase(),
				type,
				false,
				user,
				projectDetails
		);
		demoDataTestItemService.finishTestItem(beforeMethodId, status, user, projectDetails);

	}

	private StatusEnum status() {
		int STATUS_PROBABILITY = 15;
		if (ContentUtils.getWithProbability(STATUS_PROBABILITY)) {
			return SKIPPED;
		} else if (ContentUtils.getWithProbability(2 * STATUS_PROBABILITY)) {
			return FAILED;
		}
		return PASSED;
	}
}
