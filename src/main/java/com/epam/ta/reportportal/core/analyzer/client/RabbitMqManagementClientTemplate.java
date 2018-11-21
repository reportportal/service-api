package com.epam.ta.reportportal.core.analyzer.client;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;

import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class RabbitMqManagementClientTemplate implements RabbitMqManagementClient {

	public static final String ANALYZER_VHOST_NAME = "analyzer";

	private final RabbitManagementTemplate template;

	public RabbitMqManagementClientTemplate(RabbitManagementTemplate template) {
		this.template = template;
		try {
			template.getClient().createVhost(ANALYZER_VHOST_NAME);
		} catch (JsonProcessingException e) {
			throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Unable to create RabbitMq virtual host");
		}
	}

	public List<Exchange> getAnalyzerExchanges() {
		return template.getExchanges(ANALYZER_VHOST_NAME);
	}

	public Queue getAnalyzerQueue(String name) {
		return template.getQueue(ANALYZER_VHOST_NAME, name);
	}
}
