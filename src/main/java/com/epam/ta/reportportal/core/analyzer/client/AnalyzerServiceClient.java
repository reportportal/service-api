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

import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.CleanIndexRq;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.ANALYZER_KEY;
import static java.util.stream.Collectors.toList;

@Service
public class AnalyzerServiceClient implements com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient {

	private static final Logger LOGGER = LogManager.getLogger(AnalyzerServiceClient.class.getSimpleName());

	private static final String INDEX_ROUTE = "index";
	private static final String ANALYZE_ROUTE = "analyze";
	private static final String DELETE_ROUTE = "delete";
	private static final String CLEAN_ROUTE = "clean";

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public AnalyzerServiceClient(RabbitMqManagementClient rabbitMqManagementClient,
			@Qualifier("analyzerRabbitTemplate") RabbitTemplate rabbitTemplate) {
		this.rabbitMqManagementClient = rabbitMqManagementClient;
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public boolean hasClients() {
		return rabbitMqManagementClient.getAnalyzerExchangesInfo().size() != 0;
	}

	@Override
	public List<CompletableFuture<IndexRs>> index(List<IndexLaunch> rq) {
		return null;
		//		return rabbitMqManagementClient.getAnalyzerExchangesInfo()
		//				.stream()
		//				.filter(DOES_SUPPORT_INDEX)
		//				.flatMap(exchange -> rq.stream()
		//						.map(indexLaunch -> asyncRabbitTemplate.<IndexRs>convertSendAndReceive(exchange.getName(),
		//								INDEX_ROUTE,
		//								indexLaunch
		//						)))
		//				.map(future -> {
		//					CompletableFuture<IndexRs> indexed = new CompletableFuture<>();
		//					future.addCallback(indexedLaunchCallback(indexed));
		//					return indexed;
		//				})
		//				.collect(Collectors.toList());
	}

	@Override
	public Map<String, List<AnalyzedItemRs>> analyze(IndexLaunch rq) {
		LOGGER.error("I am in async supplier!!");
		List<ExchangeInfo> analyzerExchanges = rabbitMqManagementClient.getAnalyzerExchangesInfo();
		Map<String, List<AnalyzedItemRs>> resultMap = new HashMap<>(analyzerExchanges.size());
		analyzerExchanges.forEach(exchange -> analyze(rq, resultMap, exchange));
		LOGGER.error("I am returning result map size of " + resultMap.size());
		return resultMap;
	}

	@Override
	public void cleanIndex(Long index, List<Long> ids) {
		rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.forEach(exchange -> rabbitTemplate.convertAndSend(exchange.getName(), CLEAN_ROUTE, new CleanIndexRq(index, ids)));

	}

	@Override
	public void deleteIndex(Long index) {
		rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.forEach(exchange -> rabbitTemplate.convertAndSend(exchange.getName(), DELETE_ROUTE, index));
	}

	private void analyze(IndexLaunch rq, Map<String, List<AnalyzedItemRs>> resultMap, ExchangeInfo exchangeInfo) {
		List<AnalyzedItemRs> result = rabbitTemplate.convertSendAndReceiveAsType(exchangeInfo.getName(),
				ANALYZE_ROUTE,
				rq,
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
