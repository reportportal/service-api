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

package com.epam.reportportal.base.info;

import com.epam.reportportal.base.core.analyzer.auto.client.RabbitMqManagementClient;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shows list of supported analyzers
 *
 * @author Pavel Bortnik
 */
@Component
@RequiredArgsConstructor
public class AnalyzerInfoContributor implements ExtensionContributor {

  private static final String AVAILABLE_KEY = "available";

  private final RabbitMqManagementClient managementClient;

  @Override
  public Map<String, ?> contribute() {
    Set<String> exchangesWithConsumers = managementClient.getExchangesWithActiveConsumers();
    Set<Map<String, Object>> analyzersInfo = managementClient.getAnalyzerExchangesInfo()
        .stream()
        .map(exchange -> toAnalyzerInfo(exchange, exchangesWithConsumers))
        .collect(Collectors.toSet());
    return Map.of("analyzers", analyzersInfo);
  }

  private Map<String, Object> toAnalyzerInfo(ExchangeInfo exchange, Set<String> exchangesWithConsumers) {
    Map<String, Object> info = new HashMap<>(exchange.getArguments());
    info.put(AVAILABLE_KEY, exchangesWithConsumers.contains(exchange.getName()));
    return info;
  }
}
