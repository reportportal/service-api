/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.analyzer.auto.client.impl;

import static com.epam.reportportal.base.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_KEY;
import static com.epam.reportportal.base.core.analyzer.auto.client.impl.AnalyzerUtils.EXCHANGE_PRIORITY;
import static java.util.Comparator.comparingInt;

import com.epam.reportportal.base.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.BindingInfo;
import com.rabbitmq.http.client.domain.DestinationType;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import com.rabbitmq.http.client.domain.QueueInfo;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Rabbit management client filtered to analyzer exchange metadata.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Slf4j
public class RabbitMqManagementClientTemplate implements RabbitMqManagementClient {

  private final Client rabbitClient;

  private final String virtualHost;

  public RabbitMqManagementClientTemplate(Client rabbitClient, String virtualHost) {
    this.rabbitClient = rabbitClient;
    this.virtualHost = virtualHost;
    try {
      rabbitClient.createVhost(virtualHost);
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
          "Unable to create RabbitMq virtual host");
    }
  }

  @Override
  public List<ExchangeInfo> getAnalyzerExchangesInfo() {
    List<ExchangeInfo> client = rabbitClient.getExchanges(virtualHost);
    if (client == null) {
      throw new ReportPortalException(ErrorType.ANALYZER_NOT_FOUND, virtualHost);
    }
    return client.stream()
        .filter(it -> it.getArguments().get(ANALYZER_KEY) != null)
        .sorted(comparingInt(EXCHANGE_PRIORITY))
        .collect(Collectors.toList());
  }

  /**
   * Exchanges that have at least one queue bound with an active consumer.
   * Computed from a single queues fetch and a single bindings fetch to avoid per-exchange calls.
   */
  public Set<String> getExchangesWithActiveConsumers() {
    Set<String> queuesWithConsumers = CollectionUtils.emptyIfNull(rabbitClient.getQueues(virtualHost)).stream()
        .filter(queue -> queue.getConsumerCount() > 0)
        .map(QueueInfo::getName)
        .collect(Collectors.toSet());

    if (queuesWithConsumers.isEmpty()) {
      return Set.of();
    }

    return CollectionUtils.emptyIfNull(rabbitClient.getBindings(virtualHost))
        .stream()
        .filter(binding -> binding.getDestinationType() == DestinationType.QUEUE)
        .filter(binding -> queuesWithConsumers.contains(binding.getDestination()))
        .map(BindingInfo::getSource)
        .collect(Collectors.toSet());
  }
}
