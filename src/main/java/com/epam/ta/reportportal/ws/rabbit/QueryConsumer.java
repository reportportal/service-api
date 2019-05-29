package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.ws.handler.QueryHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.QUEUE_QUERY_RQ;

@Component
public class QueryConsumer {

	@Autowired
	private QueryHandler queryHandler;

	@RabbitListener(queues = QUEUE_QUERY_RQ)
	public Object find(@Payload QueryRQ queryRQ) {

		return queryHandler.find(queryRQ);
	}
}
