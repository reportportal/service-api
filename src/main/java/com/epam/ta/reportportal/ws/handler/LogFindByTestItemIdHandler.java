package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.store.database.entity.log.Log;

import java.util.List;

public interface LogFindByTestItemIdHandler {

	List<Log> findByTestItemRef(String itemRef, Integer limit, Boolean isLoadBinaryData);
}
