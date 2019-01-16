/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.client;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;

import java.util.List;
import java.util.stream.Collectors;

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
		return template.getExchanges(ANALYZER_VHOST_NAME)
				.stream()
				.filter(it -> it.getArguments().get(ANALYZER_VHOST_NAME) != null)
				.collect(Collectors.toList());
	}

	public Queue getAnalyzerQueue(String name) {
		return template.getQueue(ANALYZER_VHOST_NAME, name);
	}
}
