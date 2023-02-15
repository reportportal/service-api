package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class EmptyLogService implements LogService {

    public EmptyLogService() {
    }

    @Override
    public void saveLogMessage(Log log, Long launchId) {

    }

    @Override
    public void saveLogMessageList(List<Log> logList, Long launchId) {

    }

    @Override
    public void deleteLogMessage(Long projectId, Long logId) {

    }

    @Override
    public void deleteLogMessageByTestItemSet(Long projectId, Set<Long> itemIds) {

    }

    @Override
    public void deleteLogMessageByLaunch(Long projectId, Long launchId) {

    }

    @Override
    public void deleteLogMessageByLaunchList(Long projectId, List<Long> launchIds) {

    }

    @Override
    public void deleteLogMessageByProject(Long projectId) {

    }
}
