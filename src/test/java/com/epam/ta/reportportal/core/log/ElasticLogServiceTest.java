package com.epam.ta.reportportal.core.log;

import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_MESSAGE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.constant.LogRepositoryConstants;
import com.epam.ta.reportportal.dao.custom.ElasticSearchClient;
import com.epam.ta.reportportal.entity.item.NestedItem;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.log.LogMessage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@ExtendWith(MockitoExtension.class)
class ElasticLogServiceTest {

  private final static String MESSAGE = "e";

  private final static String PARENT_PATH = "1.2.3";

  private final static String CHILD_PATH = "1.2.3.4";
  @Mock
  private AmqpTemplate amqpTemplate;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private ElasticSearchClient elasticSearchClient;

  @Mock
  private LogRepository logRepository;

  @InjectMocks
  private ElasticLogService elasticLogService;
  private LogFull logFull;

  private LogMessage logMessage;

  @BeforeEach
  public void setUp() {
    Long itemId = 1L;
    Long launchId = 1L;
    logFull = new LogFull();
    logFull.setTestItem(new TestItem(itemId));
    logFull.setLaunch(new Launch(launchId));

    logMessage =
        new LogMessage(logFull.getId(), logFull.getLogTime(), logFull.getLogMessage(), itemId,
            launchId, logFull.getProjectId()
        );
  }

  @Test
  void saveLogMessage() {
    elasticLogService.saveLogMessage(logFull, logFull.getLaunch().getId());

    verify(amqpTemplate, times(1)).convertAndSend(eq(PROCESSING_EXCHANGE_NAME),
        eq(LOG_MESSAGE_SAVING_ROUTING_KEY), eq(logMessage)
    );
  }

  @Test
  void saveLogMessageList() {
    elasticLogService.saveLogMessageList(List.of(logFull), logFull.getLaunch().getId());

    verify(amqpTemplate, times(1)).convertAndSend(eq(PROCESSING_EXCHANGE_NAME),
        eq(LOG_MESSAGE_SAVING_ROUTING_KEY), eq(logMessage)
    );
  }

  @Test
  void findNestedByLogMessageFilter() {
    Long projectId = 1L;

    Pageable pageable = PageRequest.of(1, 5);

    Filter messageFilter = Filter.builder().withTarget(Log.class).withCondition(
        FilterCondition.builder().withSearchCriteria(CRITERIA_LOG_MESSAGE).withValue(MESSAGE)
            .withCondition(Condition.CONTAINS).build()).withCondition(
        FilterCondition.builder().withSearchCriteria(CRITERIA_PATH).withValue(PARENT_PATH)
            .withCondition(Condition.UNDER).build()).build();

    Long parentId = 3L;
    TestItem parentTestItem = new TestItem();
    parentTestItem.setItemId(parentId);
    parentTestItem.setPath(PARENT_PATH);
    parentTestItem.setLaunchId(1L);

    Long childId = 4L;
    TestItem childTestItem = new TestItem();
    childTestItem.setItemId(childId);
    childTestItem.setPath(CHILD_PATH);
    childTestItem.setLaunchId(1L);

    LogFull parentLog = new LogFull();
    parentLog.setTestItem(parentTestItem);
    parentLog.setLogMessage("Test");
    parentLog.setId(1L);
    parentLog.setLogLevel(50000);

    LogFull childLog = new LogFull();
    childLog.setTestItem(childTestItem);
    childLog.setLogMessage("Child Test");
    childLog.setId(2L);
    childLog.setLogLevel(50000);

    List<Long> childTestItems = List.of(parentId, childId);

    when(testItemRepository.selectAllDescendantsIds(PARENT_PATH)).thenReturn(childTestItems);

    when(elasticSearchClient.searchTestItemAndLogIdsByLogIdsAndString(projectId, childTestItems,
        MESSAGE
    )).thenReturn(Map.of(parentId, List.of(parentLog.getId()), childId, List.of(childLog.getId())));

    when(logRepository.findTestItemIdsByNestedLogIds(List.of(childLog.getId()), parentId))
        .thenReturn(List.of(childId));

    NestedItem nestedTestItem =
        new NestedItem(childId, LogRepositoryConstants.ITEM, 0);

    NestedItem nestedLog =
        new NestedItem(parentLog.getId(), LogRepositoryConstants.LOG, 0);

    Page<NestedItem> pageWithLogsAndItems =
        PageableExecutionUtils.getPage(List.of(nestedTestItem, nestedLog), pageable, () -> 1L);

    when(logRepository.getPageWithNestedItemsByTestItemIdsAndLogIds(eq(List.of(childId)),
        eq(List.of(parentLog.getId())), any(Pageable.class)
    )).thenReturn(pageWithLogsAndItems);

    Page<NestedItem> nestedByLogMessageFilter =
        elasticLogService.findNestedByLogMessageFilter(parentId, PARENT_PATH, projectId, 1L,
            messageFilter, pageable
        );

    Assertions.assertEquals(nestedByLogMessageFilter, pageWithLogsAndItems);
  }
}