package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.elastic.dao.LogMessageRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogMessage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class LogService {

    private final LogMessageRepository logMessageRepository;

    public LogService(LogMessageRepository logMessageRepository) {
        this.logMessageRepository = logMessageRepository;
    }

    public LogMessage saveLogMessageToElasticSearch(Log log) {
        if (Objects.isNull(log)) return null;
        return logMessageRepository.save(convertLogToLogMessage(log));
    }

    public Iterable<LogMessage> saveLogMessageListToElasticSearch(List<Log> logList) {
        if (CollectionUtils.isEmpty(logList)) return null;
        List<LogMessage> logMessageList = new ArrayList<>(logList.size());
        logList.stream().filter(Objects::nonNull).forEach(log -> logMessageList.add(convertLogToLogMessage(log)));
        return logMessageRepository.saveAll(logMessageList);
    }

    private LogMessage convertLogToLogMessage(Log log) {
        Long itemId = Objects.nonNull(log.getTestItem()) ? log.getTestItem().getItemId() : null;
        Long launchId = Objects.nonNull(log.getLaunch()) ? log.getLaunch().getId() : null;
        return new LogMessage(log.getId(), log.getLogTime(), log.getLogMessage(), itemId, launchId, log.getProjectId());
    }
}
