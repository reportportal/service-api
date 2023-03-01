package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_TO_LOG_FULL;

/**
 * It's temporary class, that's used for gracefully migration logs to Elasticsearch.
 * Will be removed in the future. That's why, for instance, some methods may be duplicated
 * with ElasticLogService instead of moving it to some parent class.
 */
@Service
public class EmptyLogService implements LogService {
    private final LogRepository logRepository;

    public EmptyLogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void saveLogMessage(LogFull logFull, Long launchId) {

    }

    @Override
    public void saveLogMessageList(List<LogFull> logFullList, Long launchId) {

    }

    @Override
    public void deleteLogMessage(Long projectId, Long logId) {

    }

    @Override
    public void deleteLogMessageByTestItemSet(Long projectId, Set<Long> itemIds) {

    }

    @Override
    public void deleteLogMessageByLaunch(Long projectId, Long launchId) {

    }

    @Override
    public void deleteLogMessageByLaunchList(Long projectId, List<Long> launchIds) {

    }

    @Override
    public void deleteLogMessageByProject(Long projectId) {

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

    private List<LogFull> wrapLogsWithLogMessages(List<Log> logList) {
        List<LogFull> logFullList = new ArrayList<>();
        // TODO: check for empty and add log message - tmp
        if (CollectionUtils.isNotEmpty(logList)) {
            logFullList = new ArrayList<>(logList.size());

            for (Log log : logList) {
                LogFull logFull = getLogFull(log);

                logFullList.add(logFull);
            }
        }

        return  logFullList;
    }

    private LogFull getLogFull(Log log) {
        return LOG_TO_LOG_FULL.apply(log);
    }
}
