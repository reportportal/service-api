package com.epam.ta.reportportal.core.log;

import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;
import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_TO_LOG_FULL;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.custom.ElasticSearchClient;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.log.LogMessage;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Primary
@Service
@ConditionalOnProperty(prefix = "rp.elasticsearch", name = "host")
public class ElasticLogService implements LogService {

  private final AmqpTemplate amqpTemplate;
  private final ElasticSearchClient elasticSearchClient;
  private final LogRepository logRepository;
  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;

  public ElasticLogService(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate,
      ElasticSearchClient elasticSearchClient, LogRepository logRepository,
      LaunchRepository launchRepository, TestItemRepository testItemRepository) {
    this.amqpTemplate = amqpTemplate;
    this.elasticSearchClient = elasticSearchClient;
    this.logRepository = logRepository;
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
  }

  public void saveLogMessage(LogFull logFull, Long launchId) {
    if (Objects.isNull(logFull)) {
      return;
    }
    amqpTemplate.convertAndSend(PROCESSING_EXCHANGE_NAME, LOG_MESSAGE_SAVING_ROUTING_KEY,
        convertLogToLogMessage(logFull, launchId)
    );
  }

  /**
   * Used only for generation demo data, that send all per message to avoid some object/collection
   * wrapping during reporting.
   *
   * @param logFullList list of {@link LogFull}
   */
  public void saveLogMessageList(List<LogFull> logFullList, Long launchId) {
    if (CollectionUtils.isEmpty(logFullList)) {
      return;
    }
    logFullList.stream().filter(Objects::nonNull)
        .forEach(logFull -> saveLogMessage(logFull, launchId));
  }

  @Override
  public void deleteLogMessage(Long projectId, Long logId) {
    elasticSearchClient.deleteLogsByLogIdAndProjectId(projectId, logId);
  }

  @Override
  public void deleteLogMessageByTestItemSet(Long projectId, Set<Long> itemIds) {
    elasticSearchClient.deleteLogsByItemSetAndProjectId(projectId, itemIds);
  }

  @Override
  public void deleteLogMessageByLaunch(Long projectId, Long launchId) {
    elasticSearchClient.deleteLogsByLaunchIdAndProjectId(projectId, launchId);
  }

  @Override
  public void deleteLogMessageByLaunchList(Long projectId, List<Long> launchIds) {
    elasticSearchClient.deleteLogsByLaunchListAndProjectId(projectId, launchIds);
  }

  @Override
  public void deleteLogMessageByProject(Long projectId) {
    elasticSearchClient.deleteLogsByProjectId(projectId);
  }

  @Override
  public Map<Long, List<IndexLog>> findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
      Long launchId, List<Long> itemIds, int logLevel) {
    Long projectId = launchRepository.findById(launchId).map(Launch::getProjectId).orElseThrow();
    Map<Long, List<IndexLog>> indexLogMap =
        logRepository.findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId,
            itemIds, logLevel
        );
    return wrapLogsWithLogMessages(projectId, indexLogMap);
  }

  private Map<Long, List<IndexLog>> wrapLogsWithLogMessages(Long projectId,
      Map<Long, List<IndexLog>> indexLogMap) {
    Map<Long, List<IndexLog>> wrappedMap = new HashMap<>();
    if (indexLogMap != null && indexLogMap.size() > 0) {
      List<Long> logIds =
          indexLogMap.values().stream().flatMap(Collection::stream).map(IndexLog::getLogId)
              .collect(Collectors.toList());
      Map<Long, LogMessage> logMessageMap =
          elasticSearchClient.getLogMessagesByProjectIdAndIds(projectId, logIds);

      wrappedMap = indexLogMap.entrySet().stream().peek(indexLogEntry -> {
        List<IndexLog> indexLogList = indexLogEntry.getValue().stream().peek(indexLog -> {
          LogMessage logMessage = logMessageMap.get(indexLog.getLogId());
          if (logMessage != null) {
            indexLog.setMessage(logMessage.getLogMessage());
          }
        }).collect(toList());
        indexLogEntry.setValue(indexLogList);
      }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    return wrappedMap;
  }

  @Override
  public List<String> findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(Long launchId, Long itemId,
      String path, Integer level) {
    Long projectId = launchRepository.findById(launchId).map(Launch::getProjectId).orElseThrow();
    List<Long> logIds =
        logRepository.findIdsByLaunchIdAndItemIdAndPathAndLevelGte(launchId, itemId, path, level);

    return elasticSearchClient.getLogMessagesByProjectIdAndIds(projectId, logIds).values().stream()
        .map(LogMessage::getLogMessage).collect(toList());
  }

  @Override
  public List<LogFull> findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId,
      List<Long> itemIds, int logLevel) {
    return wrapLogsWithLogMessages(
        logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemIds,
            logLevel
        ));
  }

  @Override
  public List<LogFull> findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId,
      Long itemId, int logLevel, int limit) {
    return wrapLogsWithLogMessages(
        logRepository.findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId,
            itemId, logLevel, limit
        ));
  }

  @Override
  public List<Log> findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(Long launchId,
      List<Long> itemIds, int limit) {
    return logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(launchId, itemIds,
        limit
    );
  }

  @Override
  public List<Log> findByTestItemId(Long itemId, int limit) {
    return logRepository.findByTestItemId(itemId, limit);
  }

  @Override
  public List<Log> findByTestItemId(Long itemId) {
    return logRepository.findByTestItemId(itemId);
  }

  @Override
  public List<LogFull> findByFilter(Queryable filter) {
    return wrapLogsWithLogMessages(logRepository.findByFilter(filter));
  }

  @Override
  // Possibly need to be refactored after filter investigation
  public Page<LogFull> findByFilter(Queryable filter, Pageable pageable) {
    Page<Log> byFilter = logRepository.findByFilter(filter, pageable);
    return byFilter.map(this::getLogFull);
  }

  @Override
  public List<LogFull> findAllById(Iterable<Long> ids) {
    return wrapLogsWithLogMessages(logRepository.findAllById(ids));
  }

  @Override
  public Optional<LogFull> findById(Long id) {
    return logRepository.findById(id).map(this::getLogFull);
  }

  @Override
  public Optional<LogFull> findByUuid(String uuid) {
    return logRepository.findByUuid(uuid).map(this::getLogFull);
  }

  @Override
  public List<Long> selectTestItemIdsByStringLogMessage(Collection<Long> itemIds, Integer logLevel,
      String string) {
    Long projectId = getProjectId(itemIds);
    List<Long> logIdsPg = testItemRepository.selectLogIdsWithLogLevelCondition(itemIds, logLevel);

    return elasticSearchClient.searchTestItemIdsByLogIdsAndString(projectId, logIdsPg, string);
  }

  @Override
  public List<Long> selectTestItemIdsUnderByStringLogMessage(Long launchId,
      Collection<Long> itemIds, Integer logLevel, String string) {
    return selectTestItemIdsUnderByLogMessage(launchId, itemIds, logLevel, string, false);
  }

  @Override
  public List<Long> selectTestItemIdsByRegexLogMessage(Collection<Long> itemIds, Integer logLevel,
      String pattern) {
    return testItemRepository.selectIdsByRegexLogMessage(itemIds, logLevel, pattern);
  }

  @Override
  public List<Long> selectTestItemIdsUnderByRegexLogMessage(Long launchId, Collection<Long> itemIds,
      Integer logLevel, String pattern) {
    return selectTestItemIdsUnderByLogMessage(launchId, itemIds, logLevel, pattern, true);
  }

  // TODO : refactoring pattern analyzer and add projectId as parameter
  // Instead of this method.
  private Long getProjectId(Collection<Long> itemIds) {
    Long id = itemIds.stream().findFirst().get();
    return testItemRepository.findById(id).map(
        testItem -> launchRepository.findById(testItem.getLaunchId()).map(Launch::getProjectId)
            .orElseThrow()).orElseThrow();
  }

  private LogMessage convertLogToLogMessage(LogFull logFull, Long launchId) {
    Long itemId = Objects.nonNull(logFull.getTestItem()) ? logFull.getTestItem().getItemId() : null;
    return new LogMessage(logFull.getId(), logFull.getLogTime(), logFull.getLogMessage(), itemId,
        launchId, logFull.getProjectId()
    );
  }

  private List<LogFull> wrapLogsWithLogMessages(List<Log> logList) {
    List<LogFull> logFullList = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(logList)) {
      // This part of code for optimization receiving all needed message from elastic
      // instead of get by single
      // And for getting message from elastic we need projectId
      // we get all message per projectId
      logFullList = new ArrayList<>(logList.size());
      Map<Long, LogMessage> logMessageMap = new HashMap<>();
      Map<Long, List<Long>> logIdsGroupByProject = logList.stream()
          .collect(groupingBy(Log::getProjectId, mapping(Log::getId, Collectors.toList())));

      for (Map.Entry<Long, List<Long>> logIdsPerProject : logIdsGroupByProject.entrySet()) {
        Long projectId = logIdsPerProject.getKey();
        List<Long> logIds = logIdsPerProject.getValue();
        logMessageMap.putAll(
            elasticSearchClient.getLogMessagesByProjectIdAndIds(projectId, logIds));
      }

      for (Log log : logList) {
        String logMessage = (logMessageMap.get(log.getId()) != null) ?
            logMessageMap.get(log.getId()).getLogMessage() : log.getLogMessage();
        LogFull logFull = getLogFull(log, logMessage);

        logFullList.add(logFull);
      }
    }

    return logFullList;
  }

  private LogFull getLogFull(Log log) {
    LogMessage logMessage =
        elasticSearchClient.getLogMessageByProjectIdAndId(log.getProjectId(), log.getId());
    String message = (logMessage != null) ? logMessage.getLogMessage() : null;

    return getLogFull(log, message);
  }

  private LogFull getLogFull(Log log, String logMessage) {
    LogFull logFull = LOG_TO_LOG_FULL.apply(log);
    if (Strings.isNotBlank(logMessage)) {
      logFull.setLogMessage(logMessage);
    }

    return logFull;
  }

  private List<Long> selectTestItemIdsUnderByLogMessage(Long launchId, Collection<Long> itemIds,
      Integer logLevel, String string, boolean selectByPattern) {
    Long projectId = getProjectId(itemIds);
    List<Long> matchedItemIds = new ArrayList<>();
    for (Long itemId : itemIds) {
      List<Long> logIdsPg =
          testItemRepository.selectLogIdsUnderWithLogLevelCondition(launchId, itemIds, logLevel);

      List<Long> nestedItemsMatchedIds;
      if (selectByPattern) {
        nestedItemsMatchedIds =
            elasticSearchClient.searchTestItemIdsByLogIdsAndRegexp(projectId, logIdsPg, string);
      } else {
        nestedItemsMatchedIds =
            elasticSearchClient.searchTestItemIdsByLogIdsAndString(projectId, logIdsPg, string);
      }

      if (CollectionUtils.isNotEmpty(nestedItemsMatchedIds)) {
        matchedItemIds.add(itemId);
      }
    }
    return matchedItemIds;
  }

}
