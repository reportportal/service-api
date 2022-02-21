package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogMessage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;

@Service
public class LogService {
    private final AmqpTemplate amqpTemplate;

    public LogService(@Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void saveLogMessageToElasticSearch(Log log) {
        if (Objects.isNull(log)) return;
        amqpTemplate.convertAndSend(PROCESSING_EXCHANGE_NAME, LOG_MESSAGE_SAVING_ROUTING_KEY, List.of(convertLogToLogMessage(log)));
    }

    public void saveLogMessageListToElasticSearch(List<Log> logList) {
        if (CollectionUtils.isEmpty(logList)) return;
        List<LogMessage> logMessageList = new ArrayList<>(logList.size());
        logList.stream().filter(Objects::nonNull).forEach(log -> logMessageList.add(convertLogToLogMessage(log)));
        amqpTemplate.convertAndSend(PROCESSING_EXCHANGE_NAME, LOG_MESSAGE_SAVING_ROUTING_KEY, logMessageList);
    }

    private LogMessage convertLogToLogMessage(Log log) {
        Long itemId = Objects.nonNull(log.getTestItem()) ? log.getTestItem().getItemId() : null;
        Long launchId = Objects.nonNull(log.getLaunch()) ? log.getLaunch().getId() : null;
        return new LogMessage(log.getId(), log.getLogTime(), log.getLogMessage(), itemId, launchId, log.getProjectId());
    }
}
