package com.epam.ta.reportportal.core.log.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_MESSAGE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.constant.LogRepositoryConstants;
import com.epam.ta.reportportal.entity.item.NestedItem;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class GetLogHandlerTest {

  private final static String MESSAGE = "e";

  private final LogRepository logRepository = mock(LogRepository.class);

  private final LogService logService = mock(LogService.class);

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final TestItemService testItemService = mock(TestItemService.class);

  private final GetLogHandler getLogHandler =
      new GetLogHandlerImpl(logRepository, logService, testItemRepository, testItemService);

  @Test
  void getLogs() {

    Long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    String wrongPath = "1";
    Filter idFilter = Filter.builder().withTarget(Log.class).withCondition(
        FilterCondition.builder().withSearchCriteria(CRITERIA_PATH).withValue(wrongPath)
            .withCondition(Condition.UNDER).build()).build();
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
    when(
        logService.findByFilter(queryableArgumentCaptor.capture(), any(Pageable.class))).thenReturn(
        Page.empty(pageable));

    getLogHandler.getLogs(correctPath, extractProjectDetails(user, "test_project"), idFilter,
        pageable
    );

    Queryable updatedFilter = queryableArgumentCaptor.getValue();

    List<ConvertibleCondition> filterConditions = updatedFilter.getFilterConditions();

    Optional<FilterCondition> launchIdCondition = filterConditions.stream()
        .flatMap(convertibleCondition -> convertibleCondition.getAllConditions().stream())
        .filter(c -> CRITERIA_ITEM_LAUNCH_ID.equals(c.getSearchCriteria())).findFirst();

    Assertions.assertTrue(launchIdCondition.isPresent());
    Assertions.assertEquals(String.valueOf(launch.getId()), launchIdCondition.get().getValue());

    Optional<FilterCondition> underPathCondition = filterConditions.stream()
        .flatMap(convertibleCondition -> convertibleCondition.getAllConditions().stream()).filter(
            c -> CRITERIA_PATH.equals(c.getSearchCriteria()) && Condition.UNDER.equals(
                c.getCondition())).findFirst();

    Assertions.assertTrue(underPathCondition.isPresent());
    Assertions.assertNotEquals(wrongPath, underPathCondition.get().getValue());
    Assertions.assertEquals(correctPath, underPathCondition.get().getValue());
  }

  @Test
  void getLogsByMessage() {

    Long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    Filter messageFilter = Filter.builder().withTarget(Log.class).withCondition(
        FilterCondition.builder().withSearchCriteria(CRITERIA_LOG_MESSAGE).withValue(MESSAGE)
            .withCondition(Condition.CONTAINS).build()).withCondition(
        FilterCondition.builder().withSearchCriteria(CRITERIA_PATH).withValue("1.2.3")
            .withCondition(Condition.UNDER).build()).build();
    Pageable pageable = PageRequest.of(1, 5);

    TestItem testItem = new TestItem();
    testItem.setItemId(3L);
    testItem.setLaunchId(1L);
    testItem.setPath("1.2.3");

    Launch launch = new Launch();
    launch.setId(1L);
    launch.setProjectId(projectId);

    LogFull log = new LogFull();
    log.setTestItem(testItem);
    log.setLogMessage("Test");
    log.setId(1L);
    log.setLogLevel(50000);

    when(testItemRepository.findById(testItem.getItemId())).thenReturn(Optional.of(testItem));
    when(testItemService.getEffectiveLaunch(testItem)).thenReturn(launch);

    NestedItem nestedItem =
        new NestedItem(log.getId(), LogRepositoryConstants.LOG, log.getLogLevel());

    Page<NestedItem> pageWithLogs =
        PageableExecutionUtils.getPage(List.of(nestedItem), pageable, () -> 1L);

    when(logService.findNestedByLogMessageFilter(eq(testItem.getItemId()), eq(testItem.getPath()),
        eq(projectId), eq(launch.getId()), any(Queryable.class), any(Pageable.class)
    )).thenReturn(pageWithLogs);

    when(logService.findAllById(Set.of(log.getId()))).thenReturn(List.of(log));

    Iterable<?> nestedItems = getLogHandler.getNestedItems(testItem.getItemId(),
        extractProjectDetails(user, "test_project"), new HashMap<>(), messageFilter, pageable
    );

    Assertions.assertEquals(((LogResource) nestedItems.iterator().next()).getId(), log.getId());
  }
}