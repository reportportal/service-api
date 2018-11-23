/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.analyzer.client;

import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.CleanIndexRq;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class AnalyzerServiceClient implements com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient {

	static final String INDEX_ROUTE = "index";
	static final String ANALYZE_ROUTE = "analyze";
	static final String DELETE_ROUTE = "delete";
	static final String CLEAN_ROUTE = "clean";

	private final RabbitMqManagementClient rabbitMqManagementClient;

	private final AsyncRabbitTemplate asyncRabbitTemplate;

	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public AnalyzerServiceClient(RabbitMqManagementClient rabbitMqManagementClient, AsyncRabbitTemplate asyncRabbitTemplate,
			RabbitTemplate rabbitTemplate) {
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
				.flatMap(exchange -> rq.stream()
						.map(indexLaunch -> asyncRabbitTemplate.<IndexRs>convertSendAndReceive(exchange.getName(),
								rabbitMqManagementClient.getAnalyzerQueue(INDEX_ROUTE).getName(),
								indexLaunch
						)))
				.map(future -> {
					CompletableFuture<IndexRs> f = new CompletableFuture<>();
					future.addCallback(new ListenableFutureCallback<IndexRs>() {
						@Override
						public void onFailure(Throwable ex) {
							f.completeExceptionally(ex);
						}

						@Override
						public void onSuccess(IndexRs result) {
							f.complete(result);
						}
					});
					return f;
				})
				.collect(Collectors.toList());
	}

	@Override
	public CompletableFuture<Map<String, List<AnalyzedItemRs>>> analyze(IndexLaunch rq) {
		return CompletableFuture.supplyAsync(() -> {
			List<Exchange> analyzerExchanges = rabbitMqManagementClient.getAnalyzerExchanges();
			analyzerExchanges.sort(Comparator.comparing(o -> ((Integer) o.getArguments().getOrDefault("priority", 10))));

			Map<String, List<AnalyzedItemRs>> resultMap = new HashMap<>(analyzerExchanges.size());

			analyzerExchanges.forEach(exchange -> {
				AsyncRabbitTemplate.RabbitConverterFuture<List<AnalyzedItemRs>> analyzed = asyncRabbitTemplate.convertSendAndReceive(exchange.getName(),
						rabbitMqManagementClient.getAnalyzerQueue(ANALYZE_ROUTE).getName(),
						rq
				);
				analyzed.addCallback(new ListenableFutureCallback<List<AnalyzedItemRs>>() {
					@Override
					public void onFailure(Throwable ex) {
						throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Cannot analyze " + rq);
					}

					@Override
					public void onSuccess(List<AnalyzedItemRs> result) {
						if (!CollectionUtils.isEmpty(result)) {
							resultMap.put(exchange.getName(), result);
							removeAnalyzedFromRq(rq, result);
						}
					}
				});
			});
			return resultMap;
		});
	}

	@Override
	public void cleanIndex(Long index, List<Long> ids) {
		rabbitMqManagementClient.getAnalyzerExchanges()
				.forEach(exchange -> rabbitTemplate.convertAndSend(exchange.getName(),
						rabbitMqManagementClient.getAnalyzerQueue(CLEAN_ROUTE).getName(),
						new CleanIndexRq(index, ids)
				));

	}

	@Override
	public void deleteIndex(Long index) {
		rabbitMqManagementClient.getAnalyzerExchanges()
				.forEach(exchange -> rabbitTemplate.convertAndSend(exchange.getName(),
						rabbitMqManagementClient.getAnalyzerQueue(DELETE_ROUTE).getName(),
						index
				));
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
