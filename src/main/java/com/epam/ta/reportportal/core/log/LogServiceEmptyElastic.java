package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceEmptyElastic implements LogService {

    public LogServiceEmptyElastic() {
    }

    @Override
    public void saveLogMessageToElasticSearch(Log log, Long launchId) {

    }

    @Override
    public void saveLogMessageListToElasticSearch(List<Log> logList, Long launchId) {

    }
}
