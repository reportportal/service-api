package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.store.database.entity.item.TestItem;

public interface TestItemFindOneHandler {

	TestItem findOne(String itemId);
}
