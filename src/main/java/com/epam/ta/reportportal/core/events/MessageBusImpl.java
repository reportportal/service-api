package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import org.springframework.amqp.core.AmqpTemplate;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_ACTIVITY;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_ATTACHMENT;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_EVENTS;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.QUEUE_ACTIVITY;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.QUEUE_ATTACHMENT_DELETE;

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
		this.amqpTemplate.convertAndSend(EXCHANGE_EVENTS, "", o);
	}

	@Override
	public void publishActivity(ActivityEvent o) {
		this.amqpTemplate.convertAndSend(EXCHANGE_ACTIVITY, QUEUE_ACTIVITY, o);
	}

	@Override
	public void publishDeleteAttachmentEvent(DeleteAttachmentEvent event) {

		amqpTemplate.convertAndSend(EXCHANGE_ATTACHMENT, QUEUE_ATTACHMENT_DELETE, event);

	}
}
