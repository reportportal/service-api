package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestRq;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.entity.enums.LogLevel.ERROR_INT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SuggestItemServiceTest {

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);

	private final LaunchRepository launchRepository = mock(LaunchRepository.class);

	private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

	private final LogRepository logRepository = mock(LogRepository.class);

	private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private final TestItemService testItemService = mock(TestItemService.class);

	private final LaunchAccessValidator launchAccessValidator = mock(LaunchAccessValidator.class);

	private final SuggestItemService searchLogService = new SuggestItemService(analyzerServiceClient,
			testItemRepository,
			projectRepository,
			testItemService,
			launchAccessValidator,
			logRepository
	);

	@Test
	void suggestItems() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		final Project project = new Project(1L, "default");

		TestItem testItem = new TestItem();
		testItem.setItemId(1L);

		TestItem relevantItem = getRelevantItem();

		Launch launch = new Launch();
		launch.setId(1L);

		final Log log = new Log();

		SuggestInfo suggestInfo = new SuggestInfo();
		suggestInfo.setRelevantItem(2L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		when(testItemRepository.findById(2L)).thenReturn(Optional.of(relevantItem));
		when(testItemService.getEffectiveLaunch(testItem)).thenReturn(launch);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launch.getId(),
				Collections.singletonList(testItem.getItemId()),
				ERROR_INT
		)).thenReturn(Collections.singletonList(log));

		when(analyzerServiceClient.searchSuggests(any(SuggestRq.class))).thenReturn(Collections.singletonList(suggestInfo));

		final List<SuggestedItem> suggestedItems = searchLogService.suggestItems(1L,
				ReportPortalUser.ProjectDetails.builder().withProjectId(1L).withProjectRole(ProjectRole.MEMBER.name()).build(),
				rpUser
		);

		Assertions.assertEquals(1, suggestedItems.size());

	}

	private TestItem getRelevantItem() {

		TestItem relevantItem = new TestItem();
		relevantItem.setItemId(2L);

		TestItemResults relevantItemRes = new TestItemResults();
		relevantItemRes.setEndTime(LocalDateTime.now());

		relevantItem.setItemResults(relevantItemRes);

		return relevantItem;
	}
}