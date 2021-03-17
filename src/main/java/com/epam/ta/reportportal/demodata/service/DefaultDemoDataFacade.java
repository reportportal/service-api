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
import com.epam.ta.reportportal.demodata.model.DemoItemMetadata;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
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
import static com.epam.ta.reportportal.demodata.service.ContentUtils.getNameFromType;
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
					List<Log> logs = demoLogsService.generateDemoLaunchLogs(launch.getUuid(), launch.getStatus());
					demoLogsService.attachFiles(logs, projectDetails.getProjectId(), launch.getUuid());
					return launch.getId();
				}, executor))
				.collect(toList());
		return futures.stream().map(CompletableFuture::join).collect(toList());
	}

	private void generateSuites(Map<String, Map<String, List<String>>> suitesStructure, int i, String launchId, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		suitesStructure.entrySet().stream().limit(i + 1L).forEach(suites -> {
			DemoItemMetadata metadata = new DemoItemMetadata().withLaunch(launchId)
					.withName(suites.getKey())
					.withType(SUITE)
					.withUser(user)
					.withProjectDetails(projectDetails);
			String suiteItemId = demoDataTestItemService.startRootItem(metadata);

			boolean generateNestedSteps = ContentUtils.getWithProbability(STORY_PROBABILITY);

			StringBuilder nestedStepIdBuilder = new StringBuilder();
			if (generateNestedSteps) {
				metadata.withName(ITEM_WITH_NESTED_STEPS_NAME).withParentId(suiteItemId).withType(TEST);
				nestedStepIdBuilder.append(demoDataTestItemService.startTestItem(metadata));
			}

			suites.getValue().forEach((key, value) -> {
				boolean generateClass = ContentUtils.getWithProbability(STORY_PROBABILITY);
				boolean generateBeforeMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);
				boolean generateAfterMethod = ContentUtils.getWithProbability(STORY_PROBABILITY);

				metadata.withName(key)
						.withParentId(nestedStepIdBuilder.length() > 0 ? nestedStepIdBuilder.toString() : suiteItemId)
						.withType(generateNestedSteps ? STEP : TEST)
						.withRetry(false);
				String testItemId = demoDataTestItemService.startTestItem(metadata);

				Optional<StatusEnum> beforeClassStatus = Optional.empty();
				if (!generateNestedSteps && generateClass) {
					beforeClassStatus = Optional.of(status());
					metadata.withParentId(testItemId).withType(BEFORE_CLASS).withName(getNameFromType(BEFORE_CLASS));
					generateStepItem(metadata, beforeClassStatus.get());
				}

				value.stream().limit(generateNestedSteps ? value.size() : i + 1).forEach(name -> {
					if (!generateNestedSteps && generateBeforeMethod) {
						metadata.withType(BEFORE_METHOD).withName(getNameFromType(BEFORE_METHOD)).withParentId(testItemId);
						generateStepItem(metadata, status());
					}

					StatusEnum status = status();
					metadata.withName(name).withType(STEP).withParentId(testItemId).withNested(generateNestedSteps);
					generateStepWithLogs(metadata, status);
					if (!generateNestedSteps) {
						generateRetries(metadata, status);
					}

					if (!generateNestedSteps && generateAfterMethod) {
						metadata.withType(AFTER_METHOD).withName(getNameFromType(AFTER_METHOD)).withParentId(testItemId);
						generateStepItem(metadata, status());
					}
				});
				if (generateNestedSteps) {
					metadata.withNested(false);
				}
				if (!generateNestedSteps && generateClass) {
					metadata.withType(AFTER_CLASS).withName(getNameFromType(AFTER_CLASS)).withParentId(suiteItemId);
					generateStepItem(metadata, status());
				}
				StatusEnum status = beforeClassStatus.orElse(FAILED);
				demoDataTestItemService.finishTestItem(testItemId, status, user, projectDetails);
				if (ContentUtils.getWithProbability(STORY_PROBABILITY)) {
					generateLogs(testItemId, launchId, status, projectDetails);
				}
			});

			if (generateNestedSteps) {
				demoDataTestItemService.finishTestItem(nestedStepIdBuilder.toString(), null, user, projectDetails);
			}

			demoDataTestItemService.finishRootItem(suiteItemId, user, projectDetails);
			if (ContentUtils.getWithProbability(STORY_PROBABILITY)) {
				generateLogs(suiteItemId, launchId, PASSED, projectDetails);
			}
		});
	}

	private void generateRetries(DemoItemMetadata metadata, StatusEnum status) {
		if (status != PASSED && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			while ((status = status()) != PASSED) {
				metadata.withRetry(true);
				generateStepWithLogs(metadata, status);
			}
		}
		metadata.withRetry(false);
	}

	private void generateStepWithLogs(DemoItemMetadata metadata, StatusEnum status) {
		String stepId = demoDataTestItemService.startTestItem(metadata);
		generateLogs(stepId, metadata.getLaunchId(), status, metadata.getProjectDetails());
		demoDataTestItemService.finishTestItem(stepId, status, metadata.getUser(), metadata.getProjectDetails());
	}

	private void generateLogs(String itemId, String launchId, StatusEnum status, ReportPortalUser.ProjectDetails projectDetails) {
		List<Log> logs = demoLogsService.generateDemoLogs(projectDetails.getProjectId(), itemId, status);
		demoLogsService.attachFiles(logs, projectDetails.getProjectId(), itemId, launchId);
	}

	private void generateStepItem(DemoItemMetadata metadata, StatusEnum status) {
		String beforeMethodId = demoDataTestItemService.startTestItem(metadata);
		demoDataTestItemService.finishTestItem(beforeMethodId, status, metadata.getUser(), metadata.getProjectDetails());

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
