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

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.DOES_SUPPORT_INDEX;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexDefectsUpdate;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexItemsRemove;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.IndexLaunchRemove;
import com.epam.ta.reportportal.model.analyzer.CleanIndexRq;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
@Slf4j
public class IndexerServiceClientImpl implements IndexerServiceClient {

  private static final String INDEX_ROUTE = "index";
  static final String DEFECT_UPDATE_ROUTE = "defect_update";
  static final String ITEM_REMOVE_ROUTE = "item_remove";
  static final String LAUNCH_REMOVE_ROUTE = "launch_remove";
  private static final String NAMESPACE_FINDER_ROUTE = "namespace_finder";
  static final String DELETE_ROUTE = "delete";
  private static final String CLEAN_ROUTE = "clean";
  private static final Integer DELETE_INDEX_SUCCESS_CODE = 1;

  private final RabbitMqManagementClient rabbitMqManagementClient;

  private final RabbitTemplate rabbitTemplate;

  public IndexerServiceClientImpl(RabbitMqManagementClient rabbitMqManagementClient,
      @Qualifier("analyzerRabbitTemplate") RabbitTemplate rabbitTemplate) {
    this.rabbitMqManagementClient = rabbitMqManagementClient;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public void index(List<IndexLaunch> rq) {
    rabbitMqManagementClient.getAnalyzerExchangesInfo().stream().filter(DOES_SUPPORT_INDEX)
        .forEach(exchange -> {
          rabbitTemplate.convertAndSend(exchange.getName(), NAMESPACE_FINDER_ROUTE, rq);
          rabbitTemplate.convertAndSend(exchange.getName(), INDEX_ROUTE, rq);
        });
  }

  @Override
  public List<Long> indexDefectsUpdate(Long projectId, Map<Long, String> itemsForIndexUpdate) {
    return rabbitMqManagementClient.getAnalyzerExchangesInfo().stream().filter(DOES_SUPPORT_INDEX)
        .flatMap(exchange -> ofNullable(
            rabbitTemplate.convertSendAndReceiveAsType(exchange.getName(), DEFECT_UPDATE_ROUTE,
                new IndexDefectsUpdate(projectId, itemsForIndexUpdate),
                new ParameterizedTypeReference<List<Long>>() {
                }
            )).orElse(Collections.emptyList()).stream()).collect(toList());
  }

  @Override
  public void indexItemsRemoveAsync(Long projectId, Collection<Long> itemsForIndexRemove) {
    rabbitMqManagementClient.getAnalyzerExchangesInfo().stream().filter(DOES_SUPPORT_INDEX).forEach(
        exchange -> rabbitTemplate.convertAndSend(exchange.getName(), ITEM_REMOVE_ROUTE,
            new IndexItemsRemove(projectId, itemsForIndexRemove)
        ));
  }

  @Override
  public void indexLaunchesRemove(Long projectId, Collection<Long> launchesForIndexRemove) {
    rabbitMqManagementClient.getAnalyzerExchangesInfo().stream().filter(DOES_SUPPORT_INDEX).forEach(
        exchange -> rabbitTemplate.convertAndSend(exchange.getName(), LAUNCH_REMOVE_ROUTE,
            new IndexLaunchRemove(projectId, launchesForIndexRemove)
        ));
  }

  @Override
  public void cleanIndex(Long index, List<Long> ids) {
    rabbitMqManagementClient.getAnalyzerExchangesInfo().forEach(
        exchange -> rabbitTemplate.convertAndSend(exchange.getName(), CLEAN_ROUTE,
            new CleanIndexRq(index, ids)
        ));
  }

  @Override
  public void deleteIndex(Long index) {
    rabbitMqManagementClient.getAnalyzerExchangesInfo().stream().map(
        exchange -> rabbitTemplate.convertSendAndReceiveAsType(exchange.getName(), DELETE_ROUTE,
            index, new ParameterizedTypeReference<Integer>() {
            }
        )).forEach(it -> {
      if (DELETE_INDEX_SUCCESS_CODE.equals(it)) {
        log.info("Successfully deleted index '{}'", index);
      } else {
        log.error("Error deleting index '{}'", index);
      }
    });
  }

  @Override
  public void deleteIndexAsync(Long index) {
    rabbitMqManagementClient.getAnalyzerExchangesInfo().forEach(
        exchange -> rabbitTemplate.convertAndSend(exchange.getName(), DELETE_ROUTE, index));
    log.info("Successfully sent message for deleting index '{}'", index);
  }
}
