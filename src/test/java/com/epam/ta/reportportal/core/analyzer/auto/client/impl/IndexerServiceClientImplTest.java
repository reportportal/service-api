package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexDefectsUpdate;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexItemsRemove;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_INDEX;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_PRIORITY;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.IndexerServiceClientImpl.*;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class IndexerServiceClientImplTest {

	private RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

	private RabbitMqManagementClient rabbitMqManagementClient = mock(RabbitMqManagementClient.class);

	private IndexerServiceClientImpl indexerServiceClient = new IndexerServiceClientImpl(rabbitMqManagementClient, rabbitTemplate);

	@Test
	void deleteIndex() {
		when(rabbitMqManagementClient.getAnalyzerExchangesInfo()).thenReturn(getExchanges());
		when(rabbitTemplate.convertSendAndReceiveAsType(AUTO_ANALYZER_KEY, DELETE_ROUTE, 1L, new ParameterizedTypeReference<Integer>() {
		})).thenReturn(1);
		indexerServiceClient.deleteIndex(1L);
		verify(rabbitTemplate, times(1)).convertSendAndReceiveAsType(AUTO_ANALYZER_KEY,
				DELETE_ROUTE,
				1L,
				new ParameterizedTypeReference<Integer>() {
				}
		);
	}

	@Test
	void indexDefectsUpdate() {
		Map<Long, String> update = Maps.newHashMap(1L, "pb001");
		IndexDefectsUpdate indexDefectsUpdate = new IndexDefectsUpdate(1L, update);
		when(rabbitMqManagementClient.getAnalyzerExchangesInfo()).thenReturn(getExchanges());
		when(rabbitTemplate.convertSendAndReceiveAsType(AUTO_ANALYZER_KEY,
				DEFECT_UPDATE_ROUTE,
				indexDefectsUpdate,
				new ParameterizedTypeReference<List<Long>>() {
				}
		)).thenReturn(Lists.emptyList());
		indexerServiceClient.indexDefectsUpdate(1L, update);
		verify(rabbitTemplate, times(1)).convertSendAndReceiveAsType(AUTO_ANALYZER_KEY,
				DEFECT_UPDATE_ROUTE,
				indexDefectsUpdate,
				new ParameterizedTypeReference<List<Long>>() {
				}
		);
	}

	@Test
	void indexItemsRemove() {
		List<Long> list = Lists.newArrayList(1L);
		IndexItemsRemove indexItemsRemove = new IndexItemsRemove(1L, list);
		when(rabbitMqManagementClient.getAnalyzerExchangesInfo()).thenReturn(getExchanges());
		doNothing().when(rabbitTemplate).convertAndSend(AUTO_ANALYZER_KEY, ITEM_REMOVE_ROUTE, indexItemsRemove);
		indexerServiceClient.indexItemsRemove(1L, list);
		verify(rabbitTemplate, times(1)).convertAndSend(AUTO_ANALYZER_KEY, ITEM_REMOVE_ROUTE, indexItemsRemove);
	}

	private List<ExchangeInfo> getExchanges() {
		ExchangeInfo exchangeInfo = new ExchangeInfo();
		Map<String, Object> params = new HashMap<>();
		params.put(ANALYZER_PRIORITY, 0);
		params.put(ANALYZER_INDEX, true);
		exchangeInfo.setArguments(params);
		exchangeInfo.setName(AUTO_ANALYZER_KEY);
		return Lists.newArrayList(exchangeInfo);
	}

}