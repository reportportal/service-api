package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.model.*;
import com.epam.ta.reportportal.demodata.service.DemoDataTestItemService;
import com.epam.ta.reportportal.demodata.service.DemoLogsService;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.demodata.service.ContentUtils.getNameFromType;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;
import static java.util.Optional.ofNullable;

@Service
public class DefaultSuiteGenerator implements SuiteGenerator {

	public static final int BEFORE_AFTER_LOGS_COUNT = 2;
	public static final int SUITE_LOGS_COUNT = 3;
	public static final int TEST_LOGS_COUNT = 3;
	public static final int STEP_LOGS_COUNT = 5;

	private final DemoDataTestItemService demoDataTestItemService;
	private final DemoLogsService demoLogsService;

	@Autowired
	public DefaultSuiteGenerator(DemoDataTestItemService demoDataTestItemService, DemoLogsService demoLogsService) {
		this.demoDataTestItemService = demoDataTestItemService;
		this.demoLogsService = demoLogsService;
	}

	@Override
	public void generateSuites(Suite suite, RootMetaData rootMetaData) {

		final StatusEnum suiteStatus = StatusEnum.valueOf(suite.getStatus());

		if (suite.isHasBefore()) {
			createRootItem(BEFORE_AFTER_LOGS_COUNT, getNameFromType(BEFORE_SUITE), BEFORE_SUITE, rootMetaData, suiteStatus);
		}

		final DemoItemMetadata metadata = new DemoItemMetadata().withName(suite.getName()).withType(SUITE);
		final String suiteId = demoDataTestItemService.startRootItem(metadata, rootMetaData);

		suite.getTests().forEach(test -> {
			final StatusEnum testStatus = StatusEnum.valueOf(test.getStatus());

			generateBefore(test, suiteId, BEFORE_CLASS, testStatus, rootMetaData);
			generateTest(suiteId, rootMetaData, test, testStatus);
			generateAfter(test, suiteId, AFTER_CLASS, testStatus, rootMetaData);
		});

		demoDataTestItemService.finishTestItem(suiteId, suiteStatus, rootMetaData);
		generateLogs(SUITE_LOGS_COUNT, suiteId, suiteStatus, rootMetaData);

		if (suite.isHasAfter()) {
			createRootItem(BEFORE_AFTER_LOGS_COUNT, getNameFromType(AFTER_SUITE), AFTER_SUITE, rootMetaData, suiteStatus);
		}
	}

	private void generateTest(String suiteId, RootMetaData rootMetaData, Test test, StatusEnum testStatus) {
		final DemoItemMetadata testMetaData = getMetadata(test.getName(), TEST, suiteId).withIssue(test.getIssue());
		final String testId = demoDataTestItemService.startTestItem(testMetaData, rootMetaData);

		test.getSteps().forEach(step -> {
			final StatusEnum stepStatus = StatusEnum.valueOf(step.getStatus());
			generateBefore(step, testId, BEFORE_METHOD, stepStatus, rootMetaData);
			generateStep(rootMetaData, testId, step);
			generateAfter(step, testId, AFTER_METHOD, stepStatus, rootMetaData);
		});

		ofNullable(test.getIssue()).ifPresentOrElse(issue -> demoDataTestItemService.finishTestItem(testId,
				testStatus,
				rootMetaData,
				issue
		), () -> demoDataTestItemService.finishTestItem(testId, testStatus, rootMetaData));
		generateLogs(TEST_LOGS_COUNT, testId, testStatus, rootMetaData);
	}

	private void createRootItem(int logsCount, String name, TestItemTypeEnum type, RootMetaData rootMetaData, StatusEnum status) {
		final DemoItemMetadata beforeMetaData = new DemoItemMetadata().withName(name).withType(type);
		final String itemId = demoDataTestItemService.startRootItem(beforeMetaData, rootMetaData);
		generateLogs(logsCount, itemId, status, rootMetaData);
		demoDataTestItemService.finishTestItem(itemId, status, rootMetaData);
	}

	private void generateBefore(TestingModel testingModel, String parentId, TestItemTypeEnum type, StatusEnum status,
			RootMetaData rootMetaData) {
		if (testingModel.isHasBefore()) {
			final DemoItemMetadata metadata = getMetadata(type, parentId);
			createStep(BEFORE_AFTER_LOGS_COUNT, rootMetaData, status, metadata);
		}
	}

	private void generateAfter(TestingModel testingModel, String parentId, TestItemTypeEnum type, StatusEnum status,
			RootMetaData rootMetaData) {
		if (testingModel.isHasAfter()) {
			final DemoItemMetadata metadata = getMetadata(type, parentId);
			createStep(BEFORE_AFTER_LOGS_COUNT, rootMetaData, status, metadata);
		}
	}

	protected void generateStep(RootMetaData rootMetaData, String parentId, Step step) {
		final DemoItemMetadata stepMetaData = getMetadata(step, parentId);
		createStep(STEP_LOGS_COUNT, rootMetaData, StatusEnum.valueOf(step.getStatus()), stepMetaData);
	}

	protected DemoItemMetadata getMetadata(Step step, String parentId) {
		return new DemoItemMetadata().withName(step.getName()).withType(STEP).withParentId(parentId).withIssue(step.getIssue());
	}

	protected DemoItemMetadata getMetadata(String name, TestItemTypeEnum type, String parentId) {
		return new DemoItemMetadata().withName(name).withType(type).withParentId(parentId);
	}

	private DemoItemMetadata getMetadata(TestItemTypeEnum type, String parentId) {
		return new DemoItemMetadata().withName(getNameFromType(type)).withType(type).withParentId(parentId);
	}

	protected void createStep(int logsCount, RootMetaData rootMetaData, StatusEnum stepStatus, DemoItemMetadata stepMetaData) {
		final String stepId = demoDataTestItemService.startTestItem(stepMetaData, rootMetaData);
		generateLogs(logsCount, stepId, stepStatus, rootMetaData);
		ofNullable(stepMetaData.getIssue()).ifPresentOrElse(issue -> demoDataTestItemService.finishTestItem(stepId,
				stepStatus,
				rootMetaData,
				issue
		), () -> demoDataTestItemService.finishTestItem(stepId, stepStatus, rootMetaData));
	}

	private void generateLogs(int count, String itemId, StatusEnum status, RootMetaData rootMetaData) {
		final Long projectId = rootMetaData.getProjectDetails().getProjectId();
		List<Log> logs = demoLogsService.generateItemLogs(count, projectId, itemId, status);
		demoLogsService.attachFiles(logs, projectId, itemId, rootMetaData.getLaunchUuid());
	}

}
