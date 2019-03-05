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

package com.epam.ta.reportportal.core.analyzer.client.impl;

import com.epam.ta.reportportal.core.analyzer.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.model.CleanIndexRq;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.core.analyzer.client.impl.AnalyzerUtils.DOES_SUPPORT_INDEX;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class IndexerServiceClientImpl implements IndexerServiceClient {

	private static final String INDEX_ROUTE = "index";
	private static final String DELETE_ROUTE = "delete";
	private static final String CLEAN_ROUTE = "clean";

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final AsyncAmqpTemplate asyncRabbitTemplate;

	public IndexerServiceClientImpl(RabbitMqManagementClient rabbitMqManagementClient,
			@Qualifier("asyncAnalyzerRabbitTemplate") AsyncAmqpTemplate asyncRabbitTemplate) {
		this.rabbitMqManagementClient = rabbitMqManagementClient;
		this.asyncRabbitTemplate = asyncRabbitTemplate;
	}

	@Override
	public Long index(List<IndexLaunch> rq) {
		rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.stream()
				.filter(DOES_SUPPORT_INDEX)
				.forEach(exchange -> asyncRabbitTemplate.convertSendAndReceiveAsType(exchange.getName(),
						INDEX_ROUTE,
						rq,
						new ParameterizedTypeReference<IndexRs>() {
						}
				));
		return 0L;
	}

	@Override
	public void cleanIndex(Long index, List<Long> ids) {
		rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.forEach(exchange -> asyncRabbitTemplate.convertSendAndReceive(exchange.getName(),
						CLEAN_ROUTE,
						new CleanIndexRq(index, ids)
				));

	}

	@Override
	public void deleteIndex(Long index) {
		rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.forEach(exchange -> asyncRabbitTemplate.convertSendAndReceive(exchange.getName(), DELETE_ROUTE, index));
	}
}
