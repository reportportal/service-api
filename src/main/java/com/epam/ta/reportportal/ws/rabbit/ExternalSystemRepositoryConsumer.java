package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.store.database.entity.external.ExternalSystem;
import com.epam.ta.reportportal.ws.handler.ExternalSystemFindOneHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.core.configs.RabbitMqConfiguration.EXTERNAL_SYSTEMS_FIND_ONE;

@Component
public class ExternalSystemRepositoryConsumer {

	@Autowired
	private ExternalSystemFindOneHandler externalSystemFindOneHandler;

	@RabbitListener(queues = EXTERNAL_SYSTEMS_FIND_ONE)
	public ExternalSystem findOne(@Payload String systemId) {

		return externalSystemFindOneHandler.findById(systemId);
	}
}
