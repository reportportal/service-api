package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.entity.log.Log;
import java.util.List;
import java.util.Set;

public interface LogService {

  /**
   * launchId - temporary, need to bring log launch/testItem to normal value.
   */
  void saveLogMessage(Log log, Long launchId);

  /**
   * Used only for generation demo data, that send all per message to avoid some object/collection
   * wrapping during reporting.
   *
   * @param logList
   * @param launchId - temporary, need to bring log launch/testItem to normal value.
   */
  void saveLogMessageList(List<Log> logList, Long launchId);

  void deleteLogMessage(Long projectId, Long logId);

  void deleteLogMessageByTestItemSet(Long projectId, Set<Long> itemIds);

  void deleteLogMessageByLaunch(Long projectId, Long launchId);

  void deleteLogMessageByLaunchList(Long projectId, List<Long> launchIds);

  void deleteLogMessageByProject(Long projectId);
}
