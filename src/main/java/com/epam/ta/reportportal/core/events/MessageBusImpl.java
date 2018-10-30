package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
import org.springframework.amqp.core.AmqpTemplate;

public class MessageBusImpl implements MessageBus {

	private final AmqpTemplate amqpTemplate;

	public MessageBusImpl(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	@Override
	public void publish(String exchange, String route, Object o) {
		this.amqpTemplate.convertAndSend(exchange, route, o);
	}

	@Override
	public void publish(String route, Object o) {
		this.amqpTemplate.convertSendAndReceive(route, o);
	}

	@Override
	public void broadcastEvent(Object o) {
		this.amqpTemplate.convertAndSend(RabbitMqConfiguration.EXCHANGE_EVENTS, "", o);
	}

	@Override
	public void publishActivity(ActivityEvent o) {
		this.amqpTemplate.convertAndSend(RabbitMqConfiguration.EXCHANGE_ACTIVITY, RabbitMqConfiguration.QUEUE_ACTIVITY, o);
	}

	public void temp() {
		this.amqpTemplate.receiveAndReply()
	}

}
