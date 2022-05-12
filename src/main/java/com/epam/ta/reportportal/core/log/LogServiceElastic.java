package com.epam.ta.reportportal.core.log;

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

import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;

@Primary
@Service
@ConditionalOnProperty(prefix = "rp.elasticsearchLogmessage", name = "host")
public class LogServiceElastic implements LogService {
    private final AmqpTemplate amqpTemplate;

    public LogServiceElastic(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void saveLogMessageToElasticSearch(Log log, Long launchId) {
        if (Objects.isNull(log)) return;
        amqpTemplate.convertAndSend(PROCESSING_EXCHANGE_NAME, LOG_MESSAGE_SAVING_ROUTING_KEY,
                convertLogToLogMessage(log, launchId));
    }

    /**
     * Used only for generation demo data, that send all per message to avoid some object/collection wrapping
     * during reporting.
     * @param logList
     */
    public void saveLogMessageListToElasticSearch(List<Log> logList, Long launchId) {
        if (CollectionUtils.isEmpty(logList)) return;
        logList.stream().filter(Objects::nonNull).forEach(log -> saveLogMessageToElasticSearch(log, launchId));
    }

    private LogMessage convertLogToLogMessage(Log log, Long launchId) {
        Long itemId = Objects.nonNull(log.getTestItem()) ? log.getTestItem().getItemId() : null;
        return new LogMessage(log.getId(), log.getLogTime(), log.getLogMessage(), itemId, launchId, log.getProjectId());
    }
}
