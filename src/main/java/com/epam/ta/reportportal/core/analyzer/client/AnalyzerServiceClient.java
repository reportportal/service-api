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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.ANALYZER_KEY;
import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.DOES_SUPPORT_INDEX;
import static java.util.stream.Collectors.toList;

@Service
public class AnalyzerServiceClient implements com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient {

	private static final String INDEX_ROUTE = "index";
	private static final String ANALYZE_ROUTE = "analyze";
	private static final String DELETE_ROUTE = "delete";
	private static final String CLEAN_ROUTE = "clean";

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final AsyncRabbitTemplate asyncRabbitTemplate;

	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public AnalyzerServiceClient(RabbitMqManagementClient rabbitMqManagementClient, AsyncRabbitTemplate asyncRabbitTemplate,
			@Qualifier("analyzerRabbitTemplate") RabbitTemplate rabbitTemplate) {
		this.rabbitMqManagementClient = rabbitMqManagementClient;
		this.asyncRabbitTemplate = asyncRabbitTemplate;
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public boolean hasClients() {
		return rabbitMqManagementClient.getAnalyzerExchanges().size() != 0;
	}

	@Override
	public List<CompletableFuture<IndexRs>> index(List<IndexLaunch> rq) {
		return rabbitMqManagementClient.getAnalyzerExchanges()
				.stream()
				.filter(DOES_SUPPORT_INDEX)
				.flatMap(exchange -> rq.stream()
						.map(indexLaunch -> asyncRabbitTemplate.<IndexRs>convertSendAndReceive(exchange.getName(),
								INDEX_ROUTE,
								indexLaunch
						)))
				.map(future -> {
					CompletableFuture<IndexRs> indexed = new CompletableFuture<>();
					future.addCallback(indexedLaunchCallback(indexed));
					return indexed;
				})
				.collect(Collectors.toList());
	}

	@Override
	public CompletableFuture<Map<String, List<AnalyzedItemRs>>> analyze(IndexLaunch rq) {
		return CompletableFuture.supplyAsync(() -> {
			List<Exchange> analyzerExchanges = rabbitMqManagementClient.getAnalyzerExchanges();
			Map<String, List<AnalyzedItemRs>> resultMap = new HashMap<>(analyzerExchanges.size());
			analyzerExchanges.forEach(exchange -> analyze(rq, resultMap, exchange));
			return resultMap;
		});
	}

	@Override
	public void cleanIndex(Long index, List<Long> ids) {
		rabbitMqManagementClient.getAnalyzerExchanges()
				.forEach(exchange -> rabbitTemplate.convertAndSend(exchange.getName(), CLEAN_ROUTE, new CleanIndexRq(index, ids)));

	}

	@Override
	public void deleteIndex(Long index) {
		rabbitMqManagementClient.getAnalyzerExchanges()
				.forEach(exchange -> rabbitTemplate.convertAndSend(exchange.getName(), DELETE_ROUTE, index));
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

	private void analyze(IndexLaunch rq, Map<String, List<AnalyzedItemRs>> resultMap, Exchange exchange) {
		AsyncRabbitTemplate.RabbitConverterFuture<List<AnalyzedItemRs>> analyzed = asyncRabbitTemplate.convertSendAndReceiveAsType(exchange.getName(),
				ANALYZE_ROUTE,
				rq,
				new ParameterizedTypeReference<List<AnalyzedItemRs>>() {
				}
		);
		analyzed.addCallback(analysedItemsCallback(rq, resultMap, exchange));
	}

	private ListenableFutureCallback<List<AnalyzedItemRs>> analysedItemsCallback(IndexLaunch rq,
			Map<String, List<AnalyzedItemRs>> resultMap, Exchange exchange) {
		return new ListenableFutureCallback<List<AnalyzedItemRs>>() {
			@Override
			public void onFailure(Throwable ex) {
				throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Cannot analyze " + rq);
			}

			@Override
			public void onSuccess(List<AnalyzedItemRs> result) {
				if (!CollectionUtils.isEmpty(result)) {
					resultMap.put((String) exchange.getArguments().getOrDefault(ANALYZER_KEY, exchange.getName()), result);
					removeAnalyzedFromRq(rq, result);
				}
			}
		};
	}

	private ListenableFutureCallback<IndexRs> indexedLaunchCallback(CompletableFuture<IndexRs> indexed) {
		return new ListenableFutureCallback<IndexRs>() {
			@Override
			public void onFailure(Throwable ex) {
				indexed.completeExceptionally(ex);
			}

			@Override
			public void onSuccess(IndexRs result) {
				indexed.complete(result);
			}
		};
	}
}
