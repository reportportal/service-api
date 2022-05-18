package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;

import java.util.List;

public interface LogService {

    /**
     * launchId - temporary, need to bring log launch/testItem to normal value.
     */
    void saveLogMessageToElasticSearch(Log log, Long launchId);

    /**
     * Used only for generation demo data, that send all per message to avoid some object/collection wrapping
     * during reporting.
     * @param logList
     * @param launchId - temporary, need to bring log launch/testItem to normal value.
     */
    void saveLogMessageListToElasticSearch(List<Log> logList, Long launchId);
}
