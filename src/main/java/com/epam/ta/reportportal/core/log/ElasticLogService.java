package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.custom.ElasticSearchClient;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.log.LogMessage;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;
import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_TO_LOG_FULL;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

@Primary
@Service
@ConditionalOnProperty(prefix = "rp.elasticsearch", name = "host")
public class ElasticLogService implements LogService {
    private final AmqpTemplate amqpTemplate;
    private final ElasticSearchClient elasticSearchClient;
    private final LogRepository logRepository;

    public ElasticLogService(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate, ElasticSearchClient elasticSearchClient, LogRepository logRepository) {
        this.amqpTemplate = amqpTemplate;
        this.elasticSearchClient = elasticSearchClient;
        this.logRepository = logRepository;
    }

    public void saveLogMessage(LogFull logFull, Long launchId) {
        if (Objects.isNull(logFull)) return;
        amqpTemplate.convertAndSend(PROCESSING_EXCHANGE_NAME, LOG_MESSAGE_SAVING_ROUTING_KEY,
                convertLogToLogMessage(logFull, launchId));
    }

    /**
     * Used only for generation demo data, that send all per message to avoid some object/collection wrapping
     * during reporting.
     * @param logFullList
     */
    public void saveLogMessageList(List<LogFull> logFullList, Long launchId) {
        if (CollectionUtils.isEmpty(logFullList)) return;
        logFullList.stream().filter(Objects::nonNull).forEach(logFull -> saveLogMessage(logFull, launchId));
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
    public Map<Long, List<IndexLog>> findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId, List<Long> itemIds, int logLevel) {
        return logRepository.findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemIds, logLevel);
    }

    @Override
    public List<String> findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(Long launchId, Long itemId, String path, Integer level) {
        return logRepository.findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(launchId, itemId, path, level);
    }

    @Override
    public List<LogFull> findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId, List<Long> itemIds, int logLevel) {
        return wrapLogsWithLogMessages(logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemIds, logLevel));
    }

    @Override
    public List<LogFull> findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId, Long itemId, int logLevel, int limit) {
        return wrapLogsWithLogMessages(logRepository.findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemId, logLevel, limit));
    }

    @Override
    public List<Log> findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(Long launchId, List<Long> itemIds, int limit) {
        return logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(launchId, itemIds, limit);
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

    private LogMessage convertLogToLogMessage(LogFull logFull, Long launchId) {
        Long itemId = Objects.nonNull(logFull.getTestItem()) ? logFull.getTestItem().getItemId() : null;
        return new LogMessage(logFull.getId(), logFull.getLogTime(), logFull.getLogMessage(), itemId, launchId, logFull.getProjectId());
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
            Map<Long, List<Long>> logIdsGroupByProject = logList.stream().collect(
                    groupingBy(Log::getProjectId, mapping(Log::getId, Collectors.toList()))
            );

            for (Map.Entry<Long, List<Long>> logIdsPerProject : logIdsGroupByProject.entrySet()) {
                Long projectId = logIdsPerProject.getKey();
                List<Long> logIds = logIdsPerProject.getValue();
                logMessageMap.putAll(elasticSearchClient.getLogMessagesByProjectIdAndIds(projectId, logIds));
            }

            for (Log log : logList) {
                String logMessage = (logMessageMap.get(log.getId()) != null)
                        ? logMessageMap.get(log.getId()).getLogMessage() : log.getLogMessage();
                LogFull logFull = getLogFull(log, logMessage);

                logFullList.add(logFull);
            }
        }

        return  logFullList;
    }

    private LogFull getLogFull(Log log) {
        LogMessage logMessage = elasticSearchClient.getLogMessageByProjectIdAndId(log.getProjectId(), log.getId());
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

}
