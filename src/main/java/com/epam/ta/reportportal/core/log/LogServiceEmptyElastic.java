package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "rp.elasticsearchLogmessage", name = "host", matchIfMissing = true)
public class LogServiceEmptyElastic implements LogService {

    public LogServiceEmptyElastic() {
    }

    public void saveLogMessageToElasticSearch(Log log) {
    }

    public void saveLogMessageListToElasticSearch(List<Log> logList) {
    }
}
