package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.handler.TestItemFindOneHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.core.configs.RabbitMqConfiguration.TEST_ITEMS_FIND_ONE_QUEUE;

@Component
public class TestItemsRepositoryConsumer {

	@Autowired
	private TestItemFindOneHandler testItemFindOneHandler;

	@RabbitListener(queues = TEST_ITEMS_FIND_ONE_QUEUE)
	public TestItem findOne(@Payload String itemId) {

		return testItemFindOneHandler.findOne(itemId);
	}
}
