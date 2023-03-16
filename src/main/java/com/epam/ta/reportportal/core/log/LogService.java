package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface LogService {

    /**
     * launchId - temporary, need to bring log launch/testItem to normal value.
     */
    void saveLogMessage(LogFull logFull, Long launchId);

    /**
     * Used only for generation demo data, that send all per message to avoid some object/collection wrapping
     * during reporting.
     * @param logFullList
     * @param launchId - temporary, need to bring log launch/testItem to normal value.
     */
    void saveLogMessageList(List<LogFull> logFullList, Long launchId);

    void deleteLogMessage(Long projectId, Long logId);

    void deleteLogMessageByTestItemSet(Long projectId, Set<Long> itemIds);

    void deleteLogMessageByLaunch(Long projectId, Long launchId);

    void deleteLogMessageByLaunchList(Long projectId, List<Long> launchIds);

    void deleteLogMessageByProject(Long projectId);

    /**
     * Find logs as {@link IndexLog} under {@link TestItem} and group by {@link Log#getTestItem()} ID
     *
     * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
     * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
     * @param logLevel {@link Log#getLogLevel()}
     * @return {@link List} of {@link Log}
     */
    Map<Long, List<IndexLog>> findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
            Long launchId, List<Long> itemIds, int logLevel);

    /**
     * Retrieves log message of specified test item with log level greather or equals than {@code level}
     *
     * @param launchId @link TestItem#getLaunchId()}
     * @param itemId   ID of {@link Log#getTestItem()}
     * @param path     {@link TestItem#getPath()}
     * @param level    log level
     * @return {@link List} of {@link String} of log messages
     */
    List<String> findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(Long launchId, Long itemId, String path, Integer level);

    /**
     * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
     * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
     * @param logLevel {@link Log#getLogLevel()}
     * @return {@link List} of {@link LogFull}
     */
    List<LogFull> findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId, List<Long> itemIds, int logLevel);

    /**
     * Find n latest logs for item
     *
     * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
     * @param itemId   {@link List} of the {@link Log#getTestItem()} IDs
     * @param logLevel {@link Log#getLogLevel()}
     * @param limit    Number of logs to be fetch
     * @return {@link List} of {@link LogFull}
     */
    List<LogFull> findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(Long launchId, Long itemId, int logLevel, int limit);

    /**
     * @param launchId {@link} ID of the {@link Launch} to search {@link Log} under
     * @param itemIds  {@link List} of the {@link Log#getTestItem()} IDs
     * @param limit    Max count of {@link Log} to be loaded
     * @return {@link List} of {@link Log}
     */
    List<Log> findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(Long launchId, List<Long> itemIds, int limit);

    /**
     * Load specified number of last logs for specified test item. binaryData
     * field will be loaded if it specified in appropriate input parameter, all
     * other fields will be fully loaded.
     *
     * @param limit  Max count of logs to be loaded
     * @param itemId Test Item log belongs to
     * @return Found logs
     */
    List<Log> findByTestItemId(Long itemId, int limit);

    /**
     * Load specified number of last logs for specified test item. binaryData
     * field will be loaded if it specified in appropriate input parameter, all
     * other fields will be fully loaded.
     *
     * @param itemId Test Item log belongs to
     * @return Found logs
     */
    List<Log> findByTestItemId(Long itemId);

    /**
     * Executes query built for given filter
     *
     * @param filter Filter to build a query
     * @return List of logFulls found
     */
    List<LogFull> findByFilter(Queryable filter);

    /**
     * Executes query built for given filter and maps result for given page
     *
     * @param filter   Filter to build a query
     * @param pageable {@link Pageable}
     * @return List of logFulls found
     */
    Page<LogFull> findByFilter(Queryable filter, Pageable pageable);

    List<LogFull> findAllById(Iterable<Long> ids);

    Optional<LogFull> findById(Long id);

    Optional<LogFull> findByUuid(String uuid);

    /**
     * Select item IDs which log's level is greater than or equal to provided and log's message match to the STRING pattern
     *
     * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
     * @param logLevel {@link Log#getLogLevel()}
     * @param pattern  CASE SENSITIVE STRING pattern for log message search
     * @return The {@link List} of the {@link TestItem#getItemId()}
     */
    List<Long> selectTestItemIdsByStringLogMessage(Collection<Long> itemIds, Integer logLevel, String pattern);

    /**
     * Select item IDs which descendants' log's level is greater than or equal to provided and log's message match to the REGEX pattern
     *
     * @param launchId {@link TestItem#getLaunchId()}
     * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
     * @param logLevel {@link Log#getLogLevel()}
     * @param pattern  REGEX pattern for log message search
     * @return The {@link List} of the {@link TestItem#getItemId()}
     */
    List<Long> selectTestItemIdsUnderByStringLogMessage(Long launchId, Collection<Long> itemIds, Integer logLevel, String pattern);

    /**
     * Select item IDs which log's level is greater than or equal to provided and log's message match to the REGEX pattern
     *
     * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
     * @param logLevel {@link Log#getLogLevel()}
     * @param pattern  REGEX pattern for log message search
     * @return The {@link List} of the {@link TestItem#getItemId()}
     */
    List<Long> selectTestItemIdsByRegexLogMessage(Collection<Long> itemIds, Integer logLevel, String pattern);

    /**
     * Select item IDs which descendants' log's level is greater than or equal to provided and log's message match to the REGEX pattern
     *
     * @param launchId {@link TestItem#getLaunchId()}
     * @param itemIds  {@link Collection} of {@link TestItem#getItemId()} which logs should match
     * @param logLevel {@link Log#getLogLevel()}
     * @param pattern  REGEX pattern for log message search
     * @return The {@link List} of the {@link TestItem#getItemId()}
     */
    List<Long> selectTestItemIdsUnderByRegexLogMessage(Long launchId, Collection<Long> itemIds, Integer logLevel, String pattern);
}
