package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.LogRepository;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.ws.handler.LogFindByTestItemIdHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogFindByTestItemIdHandlerImpl implements LogFindByTestItemIdHandler {

	@Autowired
	private LogRepository logRepository;

	@Override
	public List<Log> findByTestItemRef(String itemRef, Integer limit, Boolean isLoadBinaryData) {

		return logRepository.findByTestItemId(itemRef, limit, isLoadBinaryData);
	}
}
