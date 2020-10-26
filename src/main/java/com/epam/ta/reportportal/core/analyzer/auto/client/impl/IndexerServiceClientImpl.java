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

package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.ws.model.analyzer.CleanIndexRq;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexRs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.DOES_SUPPORT_INDEX;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.EXCHANGE_PRIORITY;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class IndexerServiceClientImpl implements IndexerServiceClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexerServiceClient.class);
	private static final String INDEX_ROUTE = "index";
	private static final String NAMESPACE_FINDER_ROUTE = "namespace_finder";
	private static final String DELETE_ROUTE = "delete";
	private static final String CLEAN_ROUTE = "clean";
	private static final Integer DELETE_INDEX_SUCCESS_CODE = 1;

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final RabbitTemplate rabbitTemplate;

	public IndexerServiceClientImpl(RabbitMqManagementClient rabbitMqManagementClient,
			@Qualifier("analyzerRabbitTemplate") RabbitTemplate rabbitTemplate) {
		this.rabbitMqManagementClient = rabbitMqManagementClient;
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public Long index(List<IndexLaunch> rq) {
		return rabbitMqManagementClient.getAnalyzerExchangesInfo().stream().filter(DOES_SUPPORT_INDEX).map(exchange -> {
			rabbitTemplate.convertSendAndReceiveAsType(exchange.getName(),
					NAMESPACE_FINDER_ROUTE,
					rq,
					new ParameterizedTypeReference<IndexRs>() {
					}
			);
			return rabbitTemplate.convertSendAndReceiveAsType(exchange.getName(),
					INDEX_ROUTE,
					rq,
					new ParameterizedTypeReference<IndexRs>() {
					}
			);
		}).mapToLong(it -> {
			if (Objects.nonNull(it)) {
				return it.getTook();
			}
			return 0;
		}).sum();
	}

	@Override
	public Long cleanIndex(Long index, List<Long> ids) {
		Map<Integer, Long> priorityToCleanedLogsCountMapping = rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.stream()
				.collect(Collectors.toMap(EXCHANGE_PRIORITY::applyAsInt,
						exchange -> rabbitTemplate.convertSendAndReceiveAsType(exchange.getName(),
								CLEAN_ROUTE,
								new CleanIndexRq(index, ids),
								new ParameterizedTypeReference<Long>() {
								}
						)
				));
		return priorityToCleanedLogsCountMapping.entrySet()
				.stream()
				.min(Map.Entry.comparingByKey())
				.orElseGet(() -> new AbstractMap.SimpleEntry<Integer, Long>(0, 0L))
				.getValue();
	}

	@Override
	public void deleteIndex(Long index) {
		rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.stream()
				.map(exchange -> rabbitTemplate.convertSendAndReceiveAsType(exchange.getName(),
						DELETE_ROUTE,
						index,
						new ParameterizedTypeReference<Integer>() {
						}
				))
				.forEach(it -> {
					if (DELETE_INDEX_SUCCESS_CODE.equals(it)) {
						LOGGER.info("Successfully deleted index '{}'", index);
					} else {
						LOGGER.error("Error deleting index '{}'", index);
					}
				});
	}
}
