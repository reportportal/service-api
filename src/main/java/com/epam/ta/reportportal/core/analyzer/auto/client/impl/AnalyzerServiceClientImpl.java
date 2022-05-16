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

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestRq;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.analyzer.AnalyzedItemRs;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.SearchRq;
import com.epam.ta.reportportal.ws.model.analyzer.SearchRs;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.*;
import static java.util.stream.Collectors.toList;

@Service
public class AnalyzerServiceClientImpl implements AnalyzerServiceClient {

	private static final String ANALYZE_ROUTE = "analyze";
	private static final String SEARCH_ROUTE = "search";
	private static final String SUGGEST_ROUTE = "suggest";
	private static final String SUGGEST_INFO_ROUTE = "index_suggest_info";
	private static final String REMOVE_SUGGEST_ROUTE = "remove_suggest_info";
	private static final String CLUSTER_ROUTE = "cluster";

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final RabbitTemplate rabbitTemplate;

	private String virtualHost;

	@Autowired
	public AnalyzerServiceClientImpl(RabbitMqManagementClient rabbitMqManagementClient,
			@Qualifier("analyzerRabbitTemplate") RabbitTemplate rabbitTemplate, @Value("${rp.amqp.analyzer-vhost}") String virtualHost) {
		this.rabbitMqManagementClient = rabbitMqManagementClient;
		this.rabbitTemplate = rabbitTemplate;
		this.virtualHost = virtualHost;
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

	@Override
	public List<SearchRs> searchLogs(SearchRq rq) {
		String exchangeName = resolveExchangeName(DOES_SUPPORT_SEARCH)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"There are no analyzer services with search logs support deployed."
				));
		return rabbitTemplate.convertSendAndReceiveAsType(exchangeName, SEARCH_ROUTE, rq, new ParameterizedTypeReference<>() {
		});
	}

	@Override
	public void removeSuggest(Long projectId) {
		resolveExchangeName(DOES_SUPPORT_SUGGEST)
				.ifPresent(suggestExchange -> rabbitTemplate.convertAndSend(suggestExchange, REMOVE_SUGGEST_ROUTE, projectId));
	}

	@Override
	public List<SuggestInfo> searchSuggests(SuggestRq rq) {
		return rabbitTemplate.convertSendAndReceiveAsType(getSuggestExchangeName(), SUGGEST_ROUTE, rq, new ParameterizedTypeReference<>() {
		});
	}

	@Override
	public void handleSuggestChoice(List<SuggestInfo> suggestInfos) {
		rabbitTemplate.convertAndSend(getSuggestExchangeName(), SUGGEST_INFO_ROUTE, suggestInfos);
	}

	@Override
	public ClusterData generateClusters(GenerateClustersRq generateClustersRq) {
		final String exchangeName = resolveExchangeName(DOES_SUPPORT_CLUSTER).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services with clusters creation support deployed."
		));
		return rabbitTemplate.convertSendAndReceiveAsType(exchangeName, CLUSTER_ROUTE, generateClustersRq, new ParameterizedTypeReference<>() {
		});
	}

	private Optional<String> resolveExchangeName(Predicate<ExchangeInfo> supportCondition) {
		return rabbitMqManagementClient.getAnalyzerExchangesInfo()
				.stream()
				.filter(supportCondition)
				.min(Comparator.comparingInt(EXCHANGE_PRIORITY))
				.map(ExchangeInfo::getName);
	}

	private String getSuggestExchangeName() {
		return resolveExchangeName(DOES_SUPPORT_SUGGEST)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"There are no analyzer services with suggest items support deployed."
				));
	}

	private void analyze(IndexLaunch rq, Map<String, List<AnalyzedItemRs>> resultMap, ExchangeInfo exchangeInfo) {
		List<AnalyzedItemRs> result = rabbitTemplate.convertSendAndReceiveAsType(exchangeInfo.getName(),
				ANALYZE_ROUTE,
				Collections.singletonList(rq),
				new ParameterizedTypeReference<>() {
				}
		);
		if (!CollectionUtils.isEmpty(result)) {
			resultMap.put((String) exchangeInfo.getArguments().getOrDefault(virtualHost, exchangeInfo.getName()), result);
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
