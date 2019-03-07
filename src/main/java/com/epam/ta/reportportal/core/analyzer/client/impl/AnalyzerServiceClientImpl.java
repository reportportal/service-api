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

import com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.analyzer.client.impl.AnalyzerUtils.ANALYZER_KEY;
import static java.util.stream.Collectors.toList;

@Service
public class AnalyzerServiceClientImpl implements AnalyzerServiceClient {

	private static final String ANALYZE_ROUTE = "analyze";

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public AnalyzerServiceClientImpl(RabbitMqManagementClient rabbitMqManagementClient,
			@Qualifier("analyzerRabbitTemplate") RabbitTemplate rabbitTemplate) {
		this.rabbitMqManagementClient = rabbitMqManagementClient;
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public boolean hasClients() {
		return rabbitMqManagementClient.getAnalyzerExchangesInfo().size() != 0;
	}

	@Override
	public Map<String, List<AnalyzedItemRs>> analyze(IndexLaunch rq) {
		List<ExchangeInfo> analyzerExchanges = rabbitMqManagementClient.getAnalyzerExchangesInfo();
		Map<String, List<AnalyzedItemRs>> resultMap = new HashMap<>(analyzerExchanges.size());
		analyzerExchanges.forEach(exchange -> analyze(rq, resultMap, exchange));
		return resultMap;
	}

	private void analyze(IndexLaunch rq, Map<String, List<AnalyzedItemRs>> resultMap, ExchangeInfo exchangeInfo) {
		List<AnalyzedItemRs> result = rabbitTemplate.convertSendAndReceiveAsType(
				exchangeInfo.getName(),
				ANALYZE_ROUTE,
				Collections.singletonList(rq),
				new ParameterizedTypeReference<List<AnalyzedItemRs>>() {
				}
		);
		if (!CollectionUtils.isEmpty(result)) {
			resultMap.put((String) exchangeInfo.getArguments().getOrDefault(ANALYZER_KEY, exchangeInfo.getName()), result);
			removeAnalyzedFromRq(rq, result);
		}
	}

	/**
	 * Removes form rq analyzed items to make rq for the next analyzer.
	 *
	 * @param rq       Request
	 * @param analyzed List of analyzer items
	 */
	private void removeAnalyzedFromRq(IndexLaunch rq, List<AnalyzedItemRs> analyzed) {
		List<Long> analyzedItemIds = analyzed.stream().map(AnalyzedItemRs::getItemId).collect(toList());
		rq.getTestItems().removeIf(it -> analyzedItemIds.contains(it.getTestItemId()));
	}

}
