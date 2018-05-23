package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.store.database.entity.integration.Integration;
import com.epam.ta.reportportal.ws.handler.IntegrationFindOneHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.core.configs.RabbitMqConfiguration.EXTERNAL_SYSTEMS_FIND_ONE;

@Component
public class IntegrationRepositoryConsumer {

	@Autowired
	private IntegrationFindOneHandler integrationFindOneHandler;

	@RabbitListener(queues = EXTERNAL_SYSTEMS_FIND_ONE)
	public Integration findOne(@Payload String id) {

		return integrationFindOneHandler.findById(id);
	}
}
