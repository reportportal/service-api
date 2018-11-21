package com.epam.ta.reportportal.core.analyzer.client;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface RabbitMqManagementClient {

	List<Exchange> getAnalyzerExchanges();

	Queue getAnalyzerQueue(String name);
}
