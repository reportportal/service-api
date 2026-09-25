/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.analyzer.auto.client.RabbitMqManagementClient;
import com.google.common.collect.ImmutableMap;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyzerInfoContributorTest {

  @Mock
  private RabbitMqManagementClient managementClient;

  @InjectMocks
  private AnalyzerInfoContributor contributor;

  @Test
  void contributeShouldMarkAnalyzerAvailableWhenExchangeHasActiveConsumers() {
    // given
    ExchangeInfo exchange = analyzerExchange();
    when(managementClient.getAnalyzerExchangesInfo()).thenReturn(List.of(exchange));
    when(managementClient.getExchangesWithActiveConsumers()).thenReturn(Set.of("analyzer"));

    // when
    Set<Map<String, Object>> analyzers = (Set<Map<String, Object>>) contributor.contribute().get("analyzers");
    assertEquals(1, analyzers.size());
    Map<String, Object> analyzer = analyzers.iterator().next();

    // then
    assertEquals("5.15.5", analyzer.get("version"));
    assertEquals(true, analyzer.get("available"));
  }

  @Test
  void contributeShouldMarkAnalyzerUnavailableWhenExchangeHasNoActiveConsumers() {
    // given
    ExchangeInfo exchange = analyzerExchange();
    when(managementClient.getAnalyzerExchangesInfo()).thenReturn(List.of(exchange));
    when(managementClient.getExchangesWithActiveConsumers()).thenReturn(Set.of());

    // when
    Set<Map<String, Object>> analyzers = (Set<Map<String, Object>>) contributor.contribute().get("analyzers");
    assertEquals(1, analyzers.size());
    Map<String, Object> analyzer = analyzers.iterator().next();

    // then
    assertEquals("5.15.5", analyzer.get("version"));
    assertEquals(false, analyzer.get("available"));
  }

  private static ExchangeInfo analyzerExchange() {
    ExchangeInfo exchange = new ExchangeInfo();
    exchange.setName("analyzer");
    exchange.setArguments(ImmutableMap.of("analyzer", "analyzer", "version", "5.15.5"));
    return exchange;
  }

}
