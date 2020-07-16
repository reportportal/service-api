package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class GetLogHandlerTest {

	private final LogRepository logRepository = mock(LogRepository.class);

	private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

	private final TestItemService testItemService = mock(TestItemService.class);

	private final GetLogHandler getLogHandler = new GetLogHandlerImpl(logRepository, testItemRepository, testItemService);

	@Test
	void getLogs() {

		Long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		String wrongPath = "1";
		Filter idFilter = Filter.builder()
				.withTarget(Log.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_PATH)
						.withValue(wrongPath)
						.withCondition(Condition.UNDER)
						.build())
				.build();
		Pageable pageable = PageRequest.of(1, 5);

		TestItem testItem = new TestItem();
		testItem.setItemId(3L);
		String correctPath = "1.2.3";
		testItem.setPath(correctPath);
		testItem.setLaunchId(1L);

		Launch launch = new Launch();
		launch.setId(1L);

		when(testItemRepository.findByPath(correctPath)).thenReturn(Optional.of(testItem));
		when(testItemService.getEffectiveLaunch(testItem)).thenReturn(launch);

		ArgumentCaptor<Queryable> queryableArgumentCaptor = ArgumentCaptor.forClass(Queryable.class);
		when(logRepository.findByFilter(queryableArgumentCaptor.capture(), any(Pageable.class))).thenReturn(Page.empty(pageable));

		getLogHandler.getLogs(correctPath, extractProjectDetails(user, "test_project"), idFilter, pageable);

		Queryable updatedFilter = queryableArgumentCaptor.getValue();

		List<ConvertibleCondition> filterConditions = updatedFilter.getFilterConditions();

		Optional<FilterCondition> launchIdCondition = filterConditions.stream()
				.flatMap(convertibleCondition -> convertibleCondition.getAllConditions().stream())
				.filter(c -> CRITERIA_ITEM_LAUNCH_ID.equals(c.getSearchCriteria()))
				.findFirst();

		Assertions.assertTrue(launchIdCondition.isPresent());
		Assertions.assertEquals(String.valueOf(launch.getId()), launchIdCondition.get().getValue());

		Optional<FilterCondition> underPathCondition = filterConditions.stream()
				.flatMap(convertibleCondition -> convertibleCondition.getAllConditions().stream())
				.filter(c -> CRITERIA_PATH.equals(c.getSearchCriteria()) && Condition.UNDER.equals(c.getCondition()))
				.findFirst();

		Assertions.assertTrue(underPathCondition.isPresent());
		Assertions.assertNotEquals(wrongPath, underPathCondition.get().getValue());
		Assertions.assertEquals(correctPath, underPathCondition.get().getValue());
	}
}