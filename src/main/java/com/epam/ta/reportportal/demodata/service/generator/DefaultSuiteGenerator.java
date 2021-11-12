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

	protected final DemoDataTestItemService demoDataTestItemService;
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
			final DemoItemMetadata beforeMetaData = getMetadata(getNameFromType(BEFORE_SUITE),
					BEFORE_SUITE,
					suiteStatus,
					null
			).withLogCount(BEFORE_AFTER_LOGS_COUNT);
			createStep(beforeMetaData, rootMetaData);
		}

		final DemoItemMetadata suiteMetaData = getMetadata(suite.getName(), SUITE, suiteStatus, null);
		final String suiteId = demoDataTestItemService.startRootItem(suiteMetaData, rootMetaData);

		suite.getTests().forEach(test -> {
			final StatusEnum testStatus = StatusEnum.valueOf(test.getStatus());

			if (test.isHasBefore()) {
				final DemoItemMetadata beforeMetaData = getMetadata(getNameFromType(BEFORE_CLASS),
						BEFORE_CLASS,
						testStatus,
						suiteId
				).withLogCount(BEFORE_AFTER_LOGS_COUNT);
				createStep(beforeMetaData, rootMetaData);
			}
			generateTest(suiteId, rootMetaData, test, testStatus);
			if (test.isHasAfter()) {
				final DemoItemMetadata afterMetaData = getMetadata(getNameFromType(AFTER_CLASS),
						AFTER_CLASS,
						testStatus,
						suiteId
				).withLogCount(BEFORE_AFTER_LOGS_COUNT);
				createStep(afterMetaData, rootMetaData);
			}
		});

		demoDataTestItemService.finishTestItem(suiteId, suiteStatus, rootMetaData);
		generateLogs(SUITE_LOGS_COUNT, suiteId, suiteStatus, rootMetaData);

		if (suite.isHasAfter()) {
			createStep(getMetadata(getNameFromType(AFTER_SUITE), AFTER_SUITE, suiteStatus, null).withLogCount(BEFORE_AFTER_LOGS_COUNT),
					rootMetaData
			);
		}
	}

	protected DemoItemMetadata getMetadata(String name, TestItemTypeEnum type, StatusEnum status, String parentId) {
		return new DemoItemMetadata().withName(name).withType(type).withStatus(status).withParentId(parentId);
	}

	protected void createStep(DemoItemMetadata stepMetaData, RootMetaData rootMetaData) {
		final String stepId = ofNullable(stepMetaData.getParentId()).map(parentId -> demoDataTestItemService.startTestItem(stepMetaData,
				rootMetaData
		)).orElseGet(() -> demoDataTestItemService.startRootItem(stepMetaData, rootMetaData));

		generateLogs(stepMetaData.getLogCount(), stepId, stepMetaData.getStatus(), rootMetaData);
		ofNullable(stepMetaData.getIssue()).ifPresentOrElse(issue -> demoDataTestItemService.finishTestItem(stepId,
				stepMetaData.getStatus(),
				rootMetaData,
				issue
		), () -> demoDataTestItemService.finishTestItem(stepId, stepMetaData.getStatus(), rootMetaData));
	}

	protected void generateTest(String suiteId, RootMetaData rootMetaData, Test test, StatusEnum testStatus) {
		final String testId = startTest(suiteId, rootMetaData, test, testStatus);
		generateSteps(rootMetaData, test, testId);

		ofNullable(test.getIssue()).ifPresentOrElse(issue -> demoDataTestItemService.finishTestItem(testId,
				testStatus,
				rootMetaData,
				issue
		), () -> demoDataTestItemService.finishTestItem(testId, testStatus, rootMetaData));
		generateLogs(TEST_LOGS_COUNT, testId, testStatus, rootMetaData);
	}

	protected String startTest(String suiteId, RootMetaData rootMetaData, Test test, StatusEnum testStatus) {
		final DemoItemMetadata testMetaData = getMetadata(test.getName(), TEST, testStatus, suiteId).withIssue(test.getIssue());
		return demoDataTestItemService.startTestItem(testMetaData, rootMetaData);
	}

	private void generateSteps(RootMetaData rootMetaData, Test test, String testId) {
		test.getSteps().forEach(step -> {
			final StatusEnum stepStatus = StatusEnum.valueOf(step.getStatus());
			if (step.isHasBefore()) {
				final DemoItemMetadata beforeMetaData = getMetadata(getNameFromType(BEFORE_METHOD),
						BEFORE_METHOD,
						stepStatus,
						testId
				).withLogCount(BEFORE_AFTER_LOGS_COUNT);
				createStep(beforeMetaData, rootMetaData);
			}
			final DemoItemMetadata stepMetaData = getMetadata(step.getName(), STEP, stepStatus, testId).withLogCount(STEP_LOGS_COUNT)
					.withIssue(step.getIssue());
			createStep(stepMetaData, rootMetaData);
			if (step.isHasBefore()) {
				final DemoItemMetadata afterMetaData = getMetadata(getNameFromType(AFTER_METHOD),
						AFTER_METHOD,
						stepStatus,
						testId
				).withLogCount(BEFORE_AFTER_LOGS_COUNT);
				createStep(afterMetaData, rootMetaData);
			}
		});
	}

	private void generateLogs(int count, String itemId, StatusEnum status, RootMetaData rootMetaData) {
		final Long projectId = rootMetaData.getProjectDetails().getProjectId();
		List<Log> logs = demoLogsService.generateItemLogs(count, projectId, itemId, status);
		demoLogsService.attachFiles(logs, projectId, itemId, rootMetaData.getLaunchUuid());
	}

}
