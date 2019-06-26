/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.*;

public class MessageBusImpl implements MessageBus {

	private final AmqpTemplate amqpTemplate;

	private final AsyncAmqpTemplate asyncAmqpTemplate;

	public MessageBusImpl(AmqpTemplate amqpTemplate, AsyncAmqpTemplate asyncAmqpTemplate) {
		this.amqpTemplate = amqpTemplate;
		this.asyncAmqpTemplate = asyncAmqpTemplate;
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

	@Override
	public void publishLaunchFinishedEvent(LaunchFinishedEvent event) {
		asyncAmqpTemplate.convertSendAndReceive(EXCHANGE_LAUNCH, QUEUE_LAUNCH_FINISHED, event)
				.addCallback(new ListenableFutureCallback<Object>() {
					@Override
					public void onFailure(Throwable ex) {
						System.err.println("Exception");
					}

					@Override
					public void onSuccess(Object result) {
						System.err.println("Success");
					}
				});
	}

}
