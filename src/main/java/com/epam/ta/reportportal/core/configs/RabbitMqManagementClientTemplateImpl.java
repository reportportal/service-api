package com.epam.ta.reportportal.core.configs;

import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class RabbitMqManagementClientTemplateImpl implements RabbitMqManagementClient {

	private final RabbitManagementTemplate template;

	public RabbitMqManagementClientTemplateImpl(RabbitManagementTemplate template) {
		this.template = template;
	}
}
