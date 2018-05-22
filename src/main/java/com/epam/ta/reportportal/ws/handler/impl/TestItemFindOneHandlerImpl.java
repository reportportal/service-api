package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.handler.TestItemFindOneHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestItemFindOneHandlerImpl implements TestItemFindOneHandler {

	@Autowired
	private TestItemRepository testItemRepository;

	@Override
	public TestItem findOne(String itemId) {

		return testItemRepository.findById(Long.valueOf(itemId)).orElse(null);
	}
}
