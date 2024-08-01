/*
 * Copyright 2023 EPAM Systems
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
package com.epam.ta.reportportal.reporting.async;

import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.DEFAULT_QUEUE_ROUTING_KEY;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_EXCHANGE;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_QUEUE_PREFIX;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import com.rabbitmq.http.client.domain.ShovelDetails;
import com.rabbitmq.http.client.domain.ShovelInfo;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class OutdatedQueuesManagementJob {

  private final Client managementClient;

  private final List<String> queues;

  private final String address;

  private final String vhost;

  public OutdatedQueuesManagementJob(Client managementClient,
      @Qualifier("reportingQueues") List<Queue> currentReportingQueues,
      @Value("${rp.amqp.addresses}") String address,
      @Value("${rp.amqp.base-vhost}") String virtualHost) {
    this.managementClient = managementClient;
    this.queues = currentReportingQueues.stream().map(Queue::getName).collect(Collectors.toList());
    this.address = address;
    this.vhost = virtualHost;
  }


  @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
  public void run() {
    var idleQueues = getIdleQueues();
    idleQueues.forEach(
        q -> managementClient.unbindQueue(q.getVhost(), q.getName(), REPORTING_EXCHANGE,
            DEFAULT_QUEUE_ROUTING_KEY));

    idleQueues.forEach(q -> {
      if (q.getMessagesReady() > 0) {
        var shovelDetails = new ShovelDetails(address, address, 60L, false, null);
        shovelDetails.setSourceQueue(q.getName());
        shovelDetails.setSourceDeleteAfter("queue-length");
        shovelDetails.setDestinationExchange(REPORTING_EXCHANGE);
        var shovelInfo = new ShovelInfo(q.getName(), shovelDetails);
        managementClient.declareShovel(vhost, shovelInfo);
      } else {
        managementClient.deleteQueue(vhost, q.getName());
        managementClient.deleteShovel(vhost, q.getName());
      }
    });
  }

  private List<QueueInfo> getIdleQueues() {
    return managementClient.getQueues().stream()
        .filter(q -> !queues.contains(q.getName()) && q.getName().startsWith(REPORTING_QUEUE_PREFIX)
            && q.getConsumerCount() == 0).collect(
            Collectors.toList());
  }
}
