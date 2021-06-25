package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestRq;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.entity.enums.LogLevel.ERROR_INT;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.TEST_ITEM_NOT_FOUND;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
@Transactional
public class SuggestItemService {

	private static final int SUGGESTED_ITEMS_LOGS_LIMIT = 5;

	private final AnalyzerServiceClient analyzerServiceClient;
	private final TestItemRepository testItemRepository;
	private final ProjectRepository projectRepository;
	private final TestItemService testItemService;
	private final LaunchAccessValidator launchAccessValidator;
	private final LogRepository logRepository;

	public SuggestItemService(AnalyzerServiceClient analyzerServiceClient, TestItemRepository testItemRepository,
			ProjectRepository projectRepository, TestItemService testItemService, LaunchAccessValidator launchAccessValidator,
			LogRepository logRepository) {
		this.analyzerServiceClient = analyzerServiceClient;
		this.testItemRepository = testItemRepository;
		this.projectRepository = projectRepository;
		this.testItemService = testItemService;
		this.launchAccessValidator = launchAccessValidator;
		this.logRepository = logRepository;
	}

	public List<SuggestedItem> suggestItems(Long testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));

		Launch launch = testItemService.getEffectiveLaunch(testItem);
		launchAccessValidator.validate(launch.getId(), projectDetails, user);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		SuggestRq suggestRq = prepareSuggestRq(testItem, launch, project.getId(), getAnalyzerConfig(project));
		List<SuggestInfo> suggestRS = analyzerServiceClient.searchSuggests(suggestRq);
		return suggestRS.stream().map(this::prepareSuggestedItem).collect(Collectors.toList());
	}

	public void handleSuggestChoose(List<SuggestInfo> suggestInfos) {
		analyzerServiceClient.handleSuggestChoose(suggestInfos);
	}

	private SuggestedItem prepareSuggestedItem(SuggestInfo suggestInfo) {
		SuggestedItem suggestedItem = new SuggestedItem();
		suggestedItem.setSuggestRs(suggestInfo);
		TestItem relevantTestItem = testItemRepository.findById(suggestInfo.getRelevantItem())
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, "Suggested item was not found"));
		suggestedItem.setTestItemResource(TestItemConverter.TO_RESOURCE.apply(relevantTestItem));
		suggestedItem.setLogs(logRepository.findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(relevantTestItem.getLaunchId(),
				relevantTestItem.getItemId(),
				ERROR_INT,
				SUGGESTED_ITEMS_LOGS_LIMIT
		)
				.stream()
				.map(LogConverter.TO_RESOURCE)
				.collect(Collectors.toSet()));
		return suggestedItem;
	}

	private SuggestRq prepareSuggestRq(TestItem testItem, Launch launch, Long projectId, AnalyzerConfig analyzerConfig) {
		SuggestRq suggestRq = new SuggestRq();
		suggestRq.setLaunchId(launch.getId());
		suggestRq.setLaunchName(launch.getName());
		suggestRq.setTestItemId(testItem.getItemId());
		suggestRq.setUniqueId(testItem.getUniqueId());
		suggestRq.setTestCaseHash(testItem.getTestCaseHash());
		suggestRq.setProject(projectId);
		suggestRq.setAnalyzerConfig(analyzerConfig);
		suggestRq.setLogs(AnalyzerUtils.fromLogs(logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launch.getId(),
				Collections.singletonList(testItem.getItemId()),
				ERROR_INT
		)));
		return suggestRq;
	}
}
