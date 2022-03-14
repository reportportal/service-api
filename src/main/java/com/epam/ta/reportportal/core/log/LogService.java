package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;

import java.util.List;

public interface LogService {

    void saveLogMessageToElasticSearch(Log log);

    /**
     * Used only for generation demo data, that send all per message to avoid some object/collection wrapping
     * during reporting.
     * @param logList
     */
    void saveLogMessageListToElasticSearch(List<Log> logList);
}
