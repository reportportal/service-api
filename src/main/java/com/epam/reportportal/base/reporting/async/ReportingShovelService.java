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

package com.epam.reportportal.base.reporting.async;

import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.REPORTING_EXCHANGE;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.ShovelDetails;
import com.rabbitmq.http.client.domain.ShovelInfo;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Moves messages left in an abandoned reporting queue back to the reporting exchange, so that they
 * get re-hashed onto the queues of the instances that are still alive.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
public class ReportingShovelService {

  private static final long RECONNECT_DELAY_SECONDS = 60L;
  private static final String DELETE_AFTER_QUEUE_LENGTH = "queue-length";

  private final Client managementClient;
  private final String vhost;
  private final String shovelUri;

  public ReportingShovelService(Client managementClient,
      @Value("${rp.amqp.addresses}") String address,
      @Value("${rp.amqp.base-vhost}") String virtualHost) {
    this.managementClient = managementClient;
    this.vhost = virtualHost;
    this.shovelUri = shovelUri(address, virtualHost);
  }

  /**
   * Declares a shovel that republishes the current content of the given queue to the reporting
   * exchange. The shovel deletes itself once the initial queue length has been moved.
   *
   * @param queueName queue to drain, also used as the shovel name
   */
  public void republishToReportingExchange(String queueName) {
    ShovelDetails details =
        new ShovelDetails(shovelUri, shovelUri, RECONNECT_DELAY_SECONDS, false, null);
    details.setSourceQueue(queueName);
    details.setSourceDeleteAfter(DELETE_AFTER_QUEUE_LENGTH);
    details.setDestinationExchange(REPORTING_EXCHANGE);
    managementClient.declareShovel(vhost, new ShovelInfo(queueName, details));
    log.info("Declared shovel '{}' to move leftover reporting messages back to '{}'", queueName,
        REPORTING_EXCHANGE);
  }

  /**
   * Removes the shovel declared for the given queue. Missing shovels are ignored by the management
   * client.
   *
   * @param queueName queue the shovel was declared for
   */
  public void removeShovel(String queueName) {
    managementClient.deleteShovel(vhost, queueName);
  }

  private static String shovelUri(String address, String vhost) {
    String base = StringUtils.trimTrailingCharacter(address, '/');
    return base + "/" + URLEncoder.encode(vhost, StandardCharsets.UTF_8);
  }
}
