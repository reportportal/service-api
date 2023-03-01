package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestRq;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.validator.state.TestItemValidator;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.GetClusterHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.entity.enums.LogLevel.ERROR_INT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SuggestItemServiceTest {

	private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private final GetProjectHandler getProjectHandler = mock(GetProjectHandler.class);
	private final GetLaunchHandler getLaunchHandler = mock(GetLaunchHandler.class);
	private final GetClusterHandler getClusterHandler = mock(GetClusterHandler.class);

	private final LaunchAccessValidator launchAccessValidator = mock(LaunchAccessValidator.class);

	private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
	private final LogService logService = mock(LogService.class);

	private final TestItemValidator testItemValidator = mock(TestItemValidator.class);
	private final List<TestItemValidator> validators = List.of(testItemValidator);

	private final SuggestItemService suggestItemService = new SuggestItemService(analyzerServiceClient,
			getProjectHandler,
			getLaunchHandler,
			getClusterHandler,
			launchAccessValidator,
			testItemRepository,
			logService, validators
	);

	@Test
	void suggestItems() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		final Project project = new Project(1L, "default");

		TestItem testItem = new TestItem();
		testItem.setItemId(1L);
		testItem.setLaunchId(1L);

		TestItem relevantItem = getRelevantItem();

		Launch launch = new Launch();
		launch.setId(1L);

		final LogFull logFull = new LogFull();

		SuggestInfo suggestInfo = new SuggestInfo();
		suggestInfo.setRelevantItem(2L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		when(testItemValidator.validate(any(TestItem.class))).thenReturn(true);
		when(testItemRepository.findById(2L)).thenReturn(Optional.of(relevantItem));
		when(getLaunchHandler.get(testItem.getLaunchId())).thenReturn(launch);
		when(getProjectHandler.get(any(ReportPortalUser.ProjectDetails.class))).thenReturn(project);
		when(logService.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launch.getId(),
				Collections.singletonList(testItem.getItemId()),
				ERROR_INT
		)).thenReturn(Collections.singletonList(logFull));

		when(analyzerServiceClient.searchSuggests(any(SuggestRq.class))).thenReturn(Collections.singletonList(suggestInfo));

		final List<SuggestedItem> suggestedItems = suggestItemService.suggestItems(1L,
				ReportPortalUser.ProjectDetails.builder().withProjectId(1L).withProjectRole(ProjectRole.MEMBER.name()).build(),
				rpUser
		);

		Assertions.assertEquals(1, suggestedItems.size());

	}

	@Test
	void suggestRemovedItems() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		final Project project = new Project(1L, "default");

		TestItem testItem = new TestItem();
		testItem.setItemId(1L);
		testItem.setLaunchId(1L);

		Launch launch = new Launch();
		launch.setId(1L);

		SuggestInfo suggestInfo = new SuggestInfo();
		suggestInfo.setRelevantItem(2L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		when(testItemValidator.validate(any(TestItem.class))).thenReturn(true);
		when(getLaunchHandler.get(testItem.getLaunchId())).thenReturn(launch);
		when(getProjectHandler.get(any(ReportPortalUser.ProjectDetails.class))).thenReturn(project);
		when(testItemRepository.findById(2L)).thenReturn(Optional.empty());

		when(analyzerServiceClient.searchSuggests(any(SuggestRq.class))).thenReturn(Collections.singletonList(suggestInfo));

		final List<SuggestedItem> suggestedItems = suggestItemService.suggestItems(1L,
				ReportPortalUser.ProjectDetails.builder().withProjectId(1L).withProjectRole(ProjectRole.MEMBER.name()).build(),
				rpUser
		);

		Assertions.assertTrue(suggestedItems.isEmpty());

	}

	@Test
	void showThrowExceptionWhenNotValid() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem testItem = new TestItem();
		testItem.setItemId(1L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		when(testItemValidator.validate(testItem)).thenReturn(false);
		when(testItemValidator.provide(testItem)).thenReturn("Test item = 1 is a nested step");

		final ReportPortalException exception = Assertions.assertThrows(ReportPortalException.class,
				() -> suggestItemService.suggestItems(1L,
						ReportPortalUser.ProjectDetails.builder().withProjectId(1L).withProjectRole(ProjectRole.MEMBER.name()).build(),
						rpUser
				)
		);

		Assertions.assertEquals("Error in handled Request. Please, check specified parameters: 'Test item = 1 is a nested step'", exception.getMessage());
	}

	@Test
	void showThrowExceptionWhenNotFound() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = Assertions.assertThrows(ReportPortalException.class,
				() -> suggestItemService.suggestItems(1L,
						ReportPortalUser.ProjectDetails.builder().withProjectId(1L).withProjectRole(ProjectRole.MEMBER.name()).build(),
						rpUser
				)
		);

		Assertions.assertEquals("Test Item '1' not found. Did you use correct Test Item ID?", exception.getMessage());
	}

	@Test
	void suggestClusterItems() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		final Project project = new Project(1L, "default");

		final Cluster cluster = new Cluster();
		cluster.setId(1L);
		cluster.setLaunchId(1L);

		TestItem relevantItem = getRelevantItem();

		Launch launch = new Launch();
		launch.setId(1L);

		final LogFull logFull = new LogFull();

		SuggestInfo suggestInfo = new SuggestInfo();
		suggestInfo.setRelevantItem(2L);

		when(getClusterHandler.getById(1L)).thenReturn(cluster);
		when(testItemRepository.findById(2L)).thenReturn(Optional.of(relevantItem));
		when(getLaunchHandler.get(cluster.getLaunchId())).thenReturn(launch);
		when(getProjectHandler.get(any(ReportPortalUser.ProjectDetails.class))).thenReturn(project);
		when(logService.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launch.getId(),
				Collections.singletonList(relevantItem.getItemId()),
				ERROR_INT
		)).thenReturn(Collections.singletonList(logFull));

		when(analyzerServiceClient.searchSuggests(any(SuggestRq.class))).thenReturn(Collections.singletonList(suggestInfo));

		final List<SuggestedItem> suggestedItems = suggestItemService.suggestClusterItems(1L,
				ReportPortalUser.ProjectDetails.builder().withProjectId(1L).withProjectRole(ProjectRole.MEMBER.name()).build(),
				rpUser
		);

		Assertions.assertEquals(1, suggestedItems.size());

	}

	@Test
	void handleSuggestChoice() {
		final OperationCompletionRS operationCompletionRS = suggestItemService.handleSuggestChoice(new ArrayList<>());
		verify(analyzerServiceClient, times(1)).handleSuggestChoice(anyList());
		Assertions.assertEquals("User choice of suggested item was sent for handling to ML", operationCompletionRS.getResultMessage());
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