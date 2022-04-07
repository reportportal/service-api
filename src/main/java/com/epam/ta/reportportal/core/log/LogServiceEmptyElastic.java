package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceEmptyElastic implements LogService {

    public LogServiceEmptyElastic() {
    }

    public void saveLogMessageToElasticSearch(Log log) {
    }

    public void saveLogMessageListToElasticSearch(List<Log> logList) {
    }
}
