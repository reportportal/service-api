package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_INDEX;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_PRIORITY;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.IndexerServiceClientImpl.DEFECT_UPDATE_ROUTE;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.IndexerServiceClientImpl.DELETE_ROUTE;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.IndexerServiceClientImpl.ITEM_REMOVE_ROUTE;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.IndexerServiceClientImpl.LAUNCH_REMOVE_ROUTE;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexDefectsUpdate;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexItemsRemove;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexLaunchRemove;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;

class IndexerServiceClientImplTest {

  private RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

  private RabbitMqManagementClient rabbitMqManagementClient = mock(RabbitMqManagementClient.class);

  private IndexerServiceClientImpl indexerServiceClient = new IndexerServiceClientImpl(
      rabbitMqManagementClient, rabbitTemplate);

  @Test
  void deleteIndex() {
    when(rabbitMqManagementClient.getAnalyzerExchangesInfo()).thenReturn(getExchanges());
    when(rabbitTemplate.convertSendAndReceiveAsType(AUTO_ANALYZER_KEY, DELETE_ROUTE, 1L,
        new ParameterizedTypeReference<Integer>() {
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
    doNothing().when(rabbitTemplate)
        .convertAndSend(AUTO_ANALYZER_KEY, ITEM_REMOVE_ROUTE, indexItemsRemove);
    indexerServiceClient.indexItemsRemoveAsync(1L, list);
    verify(rabbitTemplate, times(1)).convertAndSend(AUTO_ANALYZER_KEY, ITEM_REMOVE_ROUTE,
        indexItemsRemove);
  }

  @Test
  void indexLaunchesRemove() {
    List<Long> list = Lists.newArrayList(1L);
    IndexLaunchRemove indexLaunchRemove = new IndexLaunchRemove(1L, list);
    when(rabbitMqManagementClient.getAnalyzerExchangesInfo()).thenReturn(getExchanges());
    doNothing().when(rabbitTemplate)
        .convertAndSend(AUTO_ANALYZER_KEY, LAUNCH_REMOVE_ROUTE, indexLaunchRemove);
    indexerServiceClient.indexLaunchesRemove(1L, list);
    verify(rabbitTemplate, times(1)).convertAndSend(AUTO_ANALYZER_KEY, LAUNCH_REMOVE_ROUTE,
        indexLaunchRemove);
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