package com.epam.ta.reportportal.core.hierarchy.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.core.item.impl.status.ChangeStatusHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.item.impl.status.ToSkippedStatusChangingStrategy.SKIPPED_ISSUE_KEY;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class FinishLaunchHierarchyHandlerTest {

	private final LaunchRepository launchRepository = mock(LaunchRepository.class);
	private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
	private final ItemAttributeRepository itemAttributeRepository = mock(ItemAttributeRepository.class);
	private final IssueTypeHandler issueTypeHandler = mock(IssueTypeHandler.class);
	private final IssueEntityRepository issueEntityRepository = mock(IssueEntityRepository.class);
	private final ChangeStatusHandler changeStatusHandler = mock(ChangeStatusHandler.class);

	private final FinishLaunchHierarchyHandler finishLaunchHierarchyHandler = new FinishLaunchHierarchyHandler(launchRepository,
			testItemRepository,
			itemAttributeRepository,
			issueTypeHandler,
			issueEntityRepository,
			changeStatusHandler
	);

	@Test
	void finishWithPassedStatus() {

		Launch launch = getLaunch();

		List<Long> idsWithChildren = Lists.newArrayList(2L, 1L);
		List<Long> idsWithoutChildren = Lists.newArrayList(3L, 4L);

		when(testItemRepository.findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(eq(launch.getId()),
				eq(StatusEnum.IN_PROGRESS),
				anyInt(),
				anyLong()
		)).thenReturn(idsWithChildren);
		when(testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(eq(launch.getId()),
				eq(StatusEnum.IN_PROGRESS),
				anyInt(),
				anyLong()
		)).thenReturn(idsWithoutChildren);

		Date endTime = Date.from(LocalDate.of(2020, Month.OCTOBER, 30).atStartOfDay(ZoneId.systemDefault()).toInstant());
		ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(testItemRepository.findAllById(idsWithChildren)).thenReturn(getTestItemsWithChildren(launch));
		when(testItemRepository.findAllById(idsWithoutChildren)).thenReturn(getTestItemsWithoutChildren(launch));

		when(issueEntityRepository.findById(3L)).thenReturn(Optional.empty());
		when(issueEntityRepository.findById(4L)).thenReturn(Optional.empty());

		finishLaunchHierarchyHandler.finishDescendants(launch,
				StatusEnum.PASSED,
				endTime,
				rpUser,
				rpUser.getProjectDetails().get(TEST_PROJECT_NAME)
		);

		verify(changeStatusHandler, times(2)).changeParentStatus(anyLong(), any(), any());
		verify(issueEntityRepository, times(0)).save(any());
	}

	@Test
	void finishWithSkippedStatus() {

		Launch launch = getLaunch();

		when(itemAttributeRepository.findByLaunchIdAndKeyAndSystem(launch.getId(),
				SKIPPED_ISSUE_KEY,
				true
		)).thenReturn(java.util.Optional.of(new ItemAttribute(SKIPPED_ISSUE_KEY, "true", true)));

		when(issueTypeHandler.defineIssueType(anyLong(), anyString())).thenReturn(getToInvestigateIssueType());

		List<Long> idsWithChildren = Lists.newArrayList(2L, 1L);
		List<Long> idsWithoutChildren = Lists.newArrayList(3L, 4L);
		when(testItemRepository.findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(eq(launch.getId()),
				eq(StatusEnum.IN_PROGRESS),
				anyInt(),
				anyLong()
		)).thenReturn(idsWithChildren);
		when(testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(eq(launch.getId()),
				eq(StatusEnum.IN_PROGRESS),
				anyInt(),
				anyLong()
		)).thenReturn(idsWithoutChildren);

		Date endTime = Date.from(LocalDate.of(2020, Month.OCTOBER, 30).atStartOfDay(ZoneId.systemDefault()).toInstant());
		ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(testItemRepository.findAllById(idsWithChildren)).thenReturn(getTestItemsWithChildren(launch));
		when(testItemRepository.findAllById(idsWithoutChildren)).thenReturn(getTestItemsWithoutChildren(launch));

		finishLaunchHierarchyHandler.finishDescendants(launch,
				StatusEnum.SKIPPED,
				endTime,
				rpUser,
				rpUser.getProjectDetails().get(TEST_PROJECT_NAME)
		);

		verify(changeStatusHandler, times(2)).changeParentStatus(anyLong(), any(), any());
		verify(issueEntityRepository, times(2)).save(any());

	}

	private Launch getLaunch() {
		Launch launch = new Launch();
		launch.setId(1L);
		return launch;
	}

	private IssueType getToInvestigateIssueType() {
		IssueType issueType = new IssueType();
		issueType.setId(1L);
		issueType.setLocator(TestItemIssueGroup.TO_INVESTIGATE.getLocator());

		IssueGroup issueGroup = new IssueGroup();
		issueGroup.setId(1);
		issueGroup.setTestItemIssueGroup(TestItemIssueGroup.TO_INVESTIGATE);
		issueType.setIssueGroup(issueGroup);
		return issueType;
	}

	private List<TestItem> getTestItemsWithChildren(Launch launch) {

		TestItem parent = new TestItem();
		parent.setItemId(1L);
		parent.setType(TestItemTypeEnum.SUITE);
		parent.setLaunchId(launch.getId());
		parent.setPath("1");
		parent.setHasStats(true);
		parent.setHasChildren(true);
		TestItemResults parentResults = new TestItemResults();
		parentResults.setStatus(StatusEnum.IN_PROGRESS);
		parent.setItemResults(parentResults);

		TestItem child = new TestItem();
		child.setItemId(2L);
		child.setType(TestItemTypeEnum.TEST);
		child.setLaunchId(launch.getId());
		child.setPath("1.2");
		child.setParent(parent);
		child.setHasStats(true);
		child.setHasChildren(true);
		TestItemResults childResults = new TestItemResults();
		childResults.setStatus(StatusEnum.IN_PROGRESS);
		child.setItemResults(childResults);

		return Lists.newArrayList(child, parent);
	}

	private List<TestItem> getTestItemsWithoutChildren(Launch launch) {

		TestItem firstChild = new TestItem();
		firstChild.setItemId(3L);
		firstChild.setType(TestItemTypeEnum.STEP);
		firstChild.setLaunchId(launch.getId());
		firstChild.setPath("1.2.3");
		firstChild.setHasStats(true);
		firstChild.setHasChildren(false);
		TestItemResults parentResults = new TestItemResults();
		parentResults.setStatus(StatusEnum.IN_PROGRESS);
		firstChild.setItemResults(parentResults);

		TestItem secondChild = new TestItem();
		secondChild.setItemId(4L);
		secondChild.setType(TestItemTypeEnum.STEP);
		secondChild.setLaunchId(launch.getId());
		secondChild.setPath("1.2.4");
		secondChild.setHasStats(true);
		secondChild.setHasChildren(true);
		TestItemResults childResults = new TestItemResults();
		childResults.setStatus(StatusEnum.IN_PROGRESS);
		secondChild.setItemResults(childResults);

		return Lists.newArrayList(firstChild, secondChild);
	}

}