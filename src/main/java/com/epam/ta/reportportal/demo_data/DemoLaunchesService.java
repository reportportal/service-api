package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.dao.LaunchMetaInfoRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
class DemoLaunchesService {

	static final String SUITES_STRUCTURE = "demo_data.json";
	private Random random = new Random();

	@Autowired
	private DemoLogsService logDemoDataService;

	@Autowired
	private DemoItemsService demoItemsService;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private LaunchMetaInfoRepository launchCounter;

	@Autowired
	@Qualifier("saveLogsTaskExecutor")
	private TaskExecutor taskExecutor;

	List<String> generateDemoLaunches(DemoDataRq demoDataRq, String user, String projectName) {
		Map<String, Map<String, List<String>>> suites;
		try {
			URL suitesStructure = this.getClass().getClassLoader().getResource(SUITES_STRUCTURE);
			if (suitesStructure == null) {
				throw new ReportPortalException("Unable to find suites description");
			}
			try (InputStreamReader fileReader = new InputStreamReader(
					new FileInputStream(this.getClass().getClassLoader().getResource(SUITES_STRUCTURE).getPath()), UTF_8)) {
				suites = new Gson().fromJson(fileReader, new TypeToken<Map<String, Map<String, List<String>>>>() {
				}.getType());
			}
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load suites description");
		}
		return generateLaunches(demoDataRq, suites, user, projectName);
	}

	private List<String> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> suitesStructure, String user,
			String project) {
		return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
			String launchId = startLaunch(rq.getLaunchName(), i, project, user);
			generateSuites(suitesStructure, launchId);
			finishLaunch(launchId);
			return launchId;
		}).collect(toList());
	}

	private List<String> generateSuites(Map<String, Map<String, List<String>>> suitesStructure, String launchId) {
		return suitesStructure.entrySet().parallelStream().limit(random.nextInt(suitesStructure.size()) + 1).map(suites -> {
			TestItem suiteItem = demoItemsService.startRootItem(suites.getKey(), launchId);
			suites.getValue().entrySet().forEach(tests -> {
				TestItem testItem = demoItemsService.startTestItem(suiteItem, launchId, tests.getKey(), TEST);
				String beforeClassStatus = "";
				if (random.nextInt(2) == 1) {
					TestItem beforeClass = demoItemsService.startTestItem(testItem, launchId, "beforeClass", BEFORE_CLASS);
					beforeClassStatus = beforeClassStatus();
					demoItemsService.finishTestItem(beforeClass.getId(), beforeClassStatus);
				}
				boolean isGenerateBeforeMethod = random.nextInt(3) == 2;
				boolean isGenerateAfterMethod = random.nextInt(3) == 2;
				tests.getValue().stream().forEach(name -> {
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
				if (random.nextInt(3) == 1) {
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
