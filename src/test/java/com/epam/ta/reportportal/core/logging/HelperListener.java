package com.epam.ta.reportportal.core.logging;

import org.springframework.amqp.core.Message;

public class HelperListener {

	@RabbitMessageLogging
	public void onMessageFull(Message message) {
	}

	@RabbitMessageLogging(logHeaders = false)
	public void onMessageWithoutHeaders(Message message) {
	}

	@RabbitMessageLogging(logBody = false)
	public void onMessageWithoutBody(Message message) {
	}
}
