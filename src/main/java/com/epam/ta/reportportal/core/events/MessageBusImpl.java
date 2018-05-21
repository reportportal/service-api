package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
import org.springframework.amqp.core.AsyncAmqpTemplate;

public class MessageBusImpl implements MessageBus {

	private final AsyncAmqpTemplate amqpTemplate;

	public MessageBusImpl(AsyncAmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	@Override
	public void send(String route, Object o) {
		this.amqpTemplate.convertSendAndReceive(route, o);
	}

	@Override
	public void broadcast(Object o) {
		this.amqpTemplate.convertSendAndReceive(RabbitMqConfiguration.EVENTS_EXCHANGE, "", o);
	}

}
