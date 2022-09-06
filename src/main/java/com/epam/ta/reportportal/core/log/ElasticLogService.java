package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.dao.custom.ElasticSearchClient;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogMessage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;

@Primary
@Service
@ConditionalOnProperty(prefix = "rp.elasticsearch", name = "host")
public class ElasticLogService implements LogService {
    private final AmqpTemplate amqpTemplate;
    private final ElasticSearchClient elasticSearchClient;

    public ElasticLogService(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate, ElasticSearchClient elasticSearchClient) {
        this.amqpTemplate = amqpTemplate;
        this.elasticSearchClient = elasticSearchClient;
    }

    public void saveLogMessage(Log log, Long launchId) {
        if (Objects.isNull(log)) return;
        amqpTemplate.convertAndSend(PROCESSING_EXCHANGE_NAME, LOG_MESSAGE_SAVING_ROUTING_KEY,
                convertLogToLogMessage(log, launchId));
    }

    /**
     * Used only for generation demo data, that send all per message to avoid some object/collection wrapping
     * during reporting.
     * @param logList
     */
    public void saveLogMessageList(List<Log> logList, Long launchId) {
        if (CollectionUtils.isEmpty(logList)) return;
        logList.stream().filter(Objects::nonNull).forEach(log -> saveLogMessage(log, launchId));
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

    private LogMessage convertLogToLogMessage(Log log, Long launchId) {
        Long itemId = Objects.nonNull(log.getTestItem()) ? log.getTestItem().getItemId() : null;
        return new LogMessage(log.getId(), log.getLogTime(), log.getLogMessage(), itemId, launchId, log.getProjectId());
    }
}
