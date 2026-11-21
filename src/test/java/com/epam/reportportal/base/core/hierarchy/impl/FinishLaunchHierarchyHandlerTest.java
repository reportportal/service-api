package com.epam.reportportal.base.core.hierarchy.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.core.item.impl.status.ToSkippedStatusChangingStrategy.SKIPPED_ISSUE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.base.core.item.impl.retry.RetryHandler;
import com.epam.reportportal.base.core.item.impl.status.ChangeStatusHandler;
import com.epam.reportportal.base.core.statistics.TestItemStatisticsService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueEntityRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class FinishLaunchHierarchyHandlerTest {

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);
  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
  private final LaunchAttributeRepository launchAttributeRepository = mock(
      LaunchAttributeRepository.class);
  private final RetryHandler retryHandler = mock(RetryHandler.class);
  private final IssueTypeHandler issueTypeHandler = mock(IssueTypeHandler.class);
  private final IssueEntityRepository issueEntityRepository = mock(IssueEntityRepository.class);
  private final ChangeStatusHandler changeStatusHandler = mock(ChangeStatusHandler.class);
  private final TestItemStatisticsService statisticsService = mock(TestItemStatisticsService.class);

  private final FinishLaunchHierarchyHandler finishLaunchHierarchyHandler = new FinishLaunchHierarchyHandler(
      launchRepository,
      testItemRepository,
      retryHandler,
      issueTypeHandler,
      issueEntityRepository,
      launchAttributeRepository,
      changeStatusHandler,
      statisticsService
  );

  @Test
  void finishWithPassedStatus() {

    Launch launch = getLaunch();

    List<Long> idsWithChildren = Lists.newArrayList(2L, 1L);
    List<Long> idsWithoutChildren = Lists.newArrayList(3L, 4L);

    when(testItemRepository.findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(
        launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(idsWithChildren);
    when(testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(idsWithoutChildren);

    Instant endTime = LocalDate.of(2020, Month.OCTOBER, 30)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant();
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    when(testItemRepository.findAllById(idsWithChildren)).thenReturn(
        getTestItemsWithChildren(launch));
    when(testItemRepository.findAllById(idsWithoutChildren)).thenReturn(
        getTestItemsWithoutChildren(launch));

    when(issueEntityRepository.findById(3L)).thenReturn(Optional.empty());
    when(issueEntityRepository.findById(4L)).thenReturn(Optional.empty());

    finishLaunchHierarchyHandler.finishDescendants(launch,
        StatusEnum.PASSED,
        endTime,
        rpUser,
        rpUserToMembership(rpUser)
    );

    verify(changeStatusHandler, times(2)).changeParentStatus(any(TestItem.class), any(), any());
    verify(issueEntityRepository, times(0)).save(any());
  }

  @Test
  void finishWithSkippedStatus() {

    Launch launch = getLaunch();

    when(launchAttributeRepository.findByLaunchIdAndKeyAndSystem(launch.getId(),
        SKIPPED_ISSUE_KEY,
        true
    )).thenReturn(java.util.Optional.of(new LaunchAttribute(SKIPPED_ISSUE_KEY, "true", true)));

    when(issueTypeHandler.defineIssueType(anyLong(), anyString())).thenReturn(
        getToInvestigateIssueType());

    List<Long> idsWithChildren = Lists.newArrayList(2L, 1L);
    List<Long> idsWithoutChildren = Lists.newArrayList(3L, 4L);
    when(testItemRepository.findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(
        launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(idsWithChildren);
    when(testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(idsWithoutChildren);

    Instant endTime = LocalDate.of(2020, Month.OCTOBER, 30)
        .atStartOfDay(ZoneId.systemDefault()).toInstant();
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    when(testItemRepository.findAllById(idsWithChildren)).thenReturn(
        getTestItemsWithChildren(launch));
    when(testItemRepository.findAllById(idsWithoutChildren)).thenReturn(
        getTestItemsWithoutChildren(launch));

    finishLaunchHierarchyHandler.finishDescendants(launch,
        StatusEnum.SKIPPED,
        endTime,
        rpUser,
        rpUserToMembership(rpUser)
    );

    verify(changeStatusHandler, times(2)).changeParentStatus(any(TestItem.class), any(), any());
    verify(issueEntityRepository, times(2)).save(any());

  }

  @Test
  void finishCollectsIdsOnceAndProcessesThemInChunks() {
    Launch launch = getLaunch();
    List<Long> leafIds = LongStream.rangeClosed(1, 120).boxed().collect(Collectors.toList());
    when(testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(leafIds);
    when(testItemRepository.findIdsByHasChildrenAndLaunchIdAndStatusOrderedByPathLevel(
        launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(List.of());
    when(testItemRepository.findAllById(anyList())).thenAnswer(invocation -> {
      List<Long> ids = invocation.getArgument(0);
      return ids.stream().map(id -> getInProgressStep(launch, id)).collect(Collectors.toList());
    });
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    int finished = finishLaunchHierarchyHandler.finishDescendants(launch, StatusEnum.PASSED,
        Instant.now(), rpUser, rpUserToMembership(rpUser));

    assertEquals(120, finished);
    verify(testItemRepository, times(1)).findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
        StatusEnum.IN_PROGRESS);
    verify(testItemRepository).findAllById(leafIds.subList(0, 50));
    verify(testItemRepository).findAllById(leafIds.subList(50, 100));
    verify(testItemRepository).findAllById(leafIds.subList(100, 120));
    verify(statisticsService, times(120)).addStatistics(any(TestItem.class));
  }

  @Test
  void finishTakesLaunchLockBeforeReadingIdsAndSkipsAlreadyFinishedItems() {
    Launch launch = getLaunch();
    TestItem finishedMeanwhile = getInProgressStep(launch, 3L);
    finishedMeanwhile.getItemResults().setStatus(StatusEnum.PASSED);
    when(testItemRepository.findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
        StatusEnum.IN_PROGRESS
    )).thenReturn(List.of(3L));
    when(testItemRepository.findAllById(List.of(3L))).thenReturn(List.of(finishedMeanwhile));
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    int finished = finishLaunchHierarchyHandler.finishDescendants(launch, StatusEnum.INTERRUPTED,
        Instant.now(), rpUser, rpUserToMembership(rpUser));

    assertEquals(0, finished);
    assertEquals(StatusEnum.PASSED, finishedMeanwhile.getItemResults().getStatus());
    verify(statisticsService, never()).addStatistics(any(TestItem.class));
    InOrder inOrder = inOrder(statisticsService, testItemRepository);
    inOrder.verify(statisticsService).acquireAdvisoryLock(launch.getId());
    inOrder.verify(testItemRepository).findIdsByNotHasChildrenAndLaunchIdAndStatus(launch.getId(),
        StatusEnum.IN_PROGRESS);
  }

  private TestItem getInProgressStep(Launch launch, Long id) {
    TestItem item = new TestItem();
    item.setItemId(id);
    item.setType(TestItemTypeEnum.STEP);
    item.setLaunchId(launch.getId());
    item.setPath("1." + id);
    item.setHasStats(true);
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.IN_PROGRESS);
    item.setItemResults(results);
    return item;
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
    child.setParentId(parent.getItemId());
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
