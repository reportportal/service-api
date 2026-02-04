package com.epam.reportportal.base.core.log.impl;

import static com.epam.reportportal.base.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.reportportal.base.util.TestProjectExtractor.extractProjectDetails;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.commons.querygen.LogFilterPreparator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.log.GetLogHandler;
import com.epam.reportportal.base.core.log.LogService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.service.LogTypeResolver;
import com.epam.reportportal.base.ws.converter.converters.LogConverter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class GetLogHandlerTest {

  private final LogRepository logRepository = mock(LogRepository.class);

  private final LogService logService = mock(LogService.class);

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final TestItemService testItemService = mock(TestItemService.class);

  private final LogConverter logConverter = mock(LogConverter.class);
  private final LogFilterPreparator logFilterPreparator = mock(LogFilterPreparator.class);
  private final LogTypeResolver logTypeResolver = mock(LogTypeResolver.class);
  private final LogSearchCollector logSearchCollector = mock(LogSearchCollector.class);

  private final GetLogHandler getLogHandler = new GetLogHandlerImpl(logRepository, logService,
      testItemRepository, testItemService, logConverter, logFilterPreparator, logTypeResolver, logSearchCollector);

  @Test
  void getLogs() {

    Long projectId = 1L;
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,
        projectId);

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
    when(logFilterPreparator.prepare(any(Filter.class), any(Long.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    ArgumentCaptor<Queryable> queryableArgumentCaptor = ArgumentCaptor.forClass(Queryable.class);
    when(
        logService.findByFilter(queryableArgumentCaptor.capture(), any(Pageable.class))).thenReturn(
        Page.empty(pageable));

    getLogHandler.getLogs(correctPath, extractProjectDetails(user, TEST_PROJECT_KEY), idFilter,
        pageable);

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
        .filter(c -> CRITERIA_PATH.equals(c.getSearchCriteria()) && Condition.UNDER.equals(
            c.getCondition()))
        .findFirst();

    Assertions.assertTrue(underPathCondition.isPresent());
    Assertions.assertNotEquals(wrongPath, underPathCondition.get().getValue());
    Assertions.assertEquals(correctPath, underPathCondition.get().getValue());
  }
}
