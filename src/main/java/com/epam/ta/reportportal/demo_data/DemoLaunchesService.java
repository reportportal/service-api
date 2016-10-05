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

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.dao.LaunchMetaInfoRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
class DemoLaunchesService {

	static final String NAME = "Demo Api Tests";
	private Random random = new Random();
	@Autowired
	private DemoLogsService logDemoDataService;
	@Autowired
	private DemoItemsService demoItemsService;
	@Autowired
	private LaunchRepository launchRepository;
	@Autowired
	private LaunchMetaInfoRepository launchCounter;
	@Value("classpath:demo/demo_data.json")
	private Resource resource;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@Qualifier("saveLogsTaskExecutor")
	private TaskExecutor taskExecutor;

	List<String> generateDemoLaunches(DemoDataRq demoDataRq, String user, String projectName) {
		Map<String, Map<String, List<String>>> suites;
		try {
			suites = objectMapper.readValue(resource.getURL(), new TypeReference<Map<String, Map<String, List<String>>>>() {
			});
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load suites description. " + e.getMessage(), e);
		}
		return generateLaunches(demoDataRq, suites, user, projectName);
	}

	private List<String> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> suitesStructure, String user,
			String project) {
		return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
			String launchId = startLaunch(NAME + "#" + rq.getPostfix(), i, project, user);
			generateSuites(suitesStructure, i, launchId);
			finishLaunch(launchId);
			return launchId;
		}).collect(toList());
	}

	private List<String> generateSuites(Map<String, Map<String, List<String>>> suitesStructure, int i, String launchId) {
		return suitesStructure.entrySet().parallelStream().limit(i + 1).map(suites -> {
			TestItem suiteItem = demoItemsService.startRootItem(suites.getKey(), launchId);
			suites.getValue().entrySet().forEach(tests -> {
				TestItem testItem = demoItemsService.startTestItem(suiteItem, launchId, tests.getKey(), TEST);
				String beforeClassStatus = "";
				if (random.nextBoolean()) {
					TestItem beforeClass = demoItemsService.startTestItem(testItem, launchId, "beforeClass", BEFORE_CLASS);
					beforeClassStatus = beforeClassStatus();
					demoItemsService.finishTestItem(beforeClass.getId(), beforeClassStatus);
				}
				boolean isGenerateBeforeMethod = random.nextBoolean();
				boolean isGenerateAfterMethod = random.nextBoolean();
				tests.getValue().stream().limit(i + 1).forEach(name -> {
					if (isGenerateBeforeMethod) {
						demoItemsService.finishTestItem(
								demoItemsService.startTestItem(testItem, launchId, "beforeMethod", BEFORE_METHOD).getId(), status());
					}
					TestItem stepId = demoItemsService.startTestItem(testItem, launchId, name, STEP);
					String status = status();
					taskExecutor.execute(() -> {
						logDemoDataService.generateDemoLogs(stepId.getId(), status);
						demoItemsService.finishTestItem(stepId.getId(), status);
						if (isGenerateAfterMethod) {
							demoItemsService.finishTestItem(
									demoItemsService.startTestItem(testItem, launchId, "afterMethod", AFTER_METHOD).getId(), status());
						}
					});
				});
				if (random.nextBoolean()) {
					TestItem afterClass = demoItemsService.startTestItem(testItem, launchId, "afterClass", AFTER_CLASS);
					demoItemsService.finishTestItem(afterClass.getId(), status());
				}
				demoItemsService.finishTestItem(testItem.getId(), !beforeClassStatus.isEmpty() ? beforeClassStatus : "FAILED");
			});
			demoItemsService.finishRootItem(suiteItem.getId());
			return suiteItem.getId();
		}).collect(toList());
	}

	private String startLaunch(String name, int i, String project, String user) {
		Launch launch = new Launch();
		launch.setName(name);
		launch.setDescription("Demo Launch");
		launch.setStartTime(new Date());
		launch.setTags(new HashSet<>(asList("desktop", "demo", "build:3.0.1." + (i + 1))));
		launch.setStatus(IN_PROGRESS);
		launch.setUserRef(user);
		launch.setProjectRef(project);
		launch.setNumber(launchCounter.getLaunchNumber(name, project));
		launch.setMode(DEFAULT);
		return launchRepository.save(launch).getId();
	}

	private String status() {
		// magic numbers to generate a distribution of steps statuses
		int value = random.nextInt(71);
		if (value <= 5) {
			return SKIPPED.name();
		} else if (value <= 48) {
			return PASSED.name();
		} else {
			return FAILED.name();
		}
	}

	private String beforeClassStatus() {
		// magic numbers to generate a distribution of before/after statuses
		int value = random.nextInt(71);
		if (value <= 3) {
			return SKIPPED.name();
		} else if (value <= 62) {
			return PASSED.name();
		} else {
			return FAILED.name();
		}
	}

	private void finishLaunch(String launchId) {
		Launch launch = launchRepository.findOne(launchId);
		launch.setEndTime(new Date());
		launch.setStatus(getStatusFromStatistics(launch.getStatistics()));
		launchRepository.save(launch);
	}
}
