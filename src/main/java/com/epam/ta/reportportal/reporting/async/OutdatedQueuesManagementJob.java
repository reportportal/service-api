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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Safety net for reporting queues left behind by instances that died without releasing them.
 *
 * <p>A queue is only considered abandoned when it lives in the vhost of this deployment, is not
 * owned by this instance, and has been without consumers for longer than the configured grace
 * period. The grace period is what keeps the job from deleting the queues of an instance that has
 * just declared them but has not attached its consumers yet, or of an instance that is reconnecting
 * after a network glitch.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
public class OutdatedQueuesManagementJob {

  private final Client managementClient;

  private final ReportingShovelService shovelService;

  private final Set<String> ownQueues;

  private final String vhost;

  private final Duration gracePeriod;

  private final Map<String, Instant> withoutConsumersSince = new ConcurrentHashMap<>();

  public OutdatedQueuesManagementJob(Client managementClient, ReportingShovelService shovelService,
      @Qualifier("reportingQueues") List<Queue> currentReportingQueues,
      @Value("${rp.amqp.base-vhost}") String virtualHost,
      @Value("${reporting.queues.cleanup.grace-period:PT2M}") Duration gracePeriod) {
    this.managementClient = managementClient;
    this.shovelService = shovelService;
    this.ownQueues = currentReportingQueues.stream().map(Queue::getName)
        .collect(Collectors.toUnmodifiableSet());
    this.vhost = virtualHost;
    this.gracePeriod = gracePeriod;
  }

  @Scheduled(fixedDelayString = "${reporting.queues.cleanup.interval:PT1M}", initialDelayString = "${reporting.queues.cleanup.initial-delay:PT1M}")
  public void run() {
    List<QueueInfo> candidates;
    try {
      candidates = findQueuesWithoutConsumers();
    } catch (Exception e) {
      log.warn("Unable to list the reporting queues of vhost '{}'", vhost, e);
      return;
    }

    withoutConsumersSince.keySet()
        .retainAll(candidates.stream().map(QueueInfo::getName).collect(Collectors.toSet()));

    Instant now = Instant.now();
    candidates.stream().filter(queue -> graceExpired(queue.getName(), now)).forEach(this::release);
  }

  private List<QueueInfo> findQueuesWithoutConsumers() {
    List<QueueInfo> queues = managementClient.getQueues(vhost);
    if (queues == null) {
      return List.of();
    }
    return queues.stream().filter(queue -> queue.getName().startsWith(REPORTING_QUEUE_PREFIX))
        .filter(queue -> !ownQueues.contains(queue.getName()))
        .filter(queue -> queue.getConsumerCount() == 0).toList();
  }

  private boolean graceExpired(String queueName, Instant now) {
    Instant since = withoutConsumersSince.computeIfAbsent(queueName, name -> now);
    return Duration.between(since, now).compareTo(gracePeriod) >= 0;
  }

  private void release(QueueInfo queue) {
    String queueName = queue.getName();
    try {
      managementClient.unbindQueue(vhost, queueName, REPORTING_EXCHANGE, DEFAULT_QUEUE_ROUTING_KEY);
      if (queue.getMessagesReady() > 0) {
        shovelService.republishToReportingExchange(queueName);
      } else {
        shovelService.removeShovel(queueName);
        managementClient.deleteQueue(vhost, queueName);
        withoutConsumersSince.remove(queueName);
        log.info("Removed outdated reporting queue '{}'", queueName);
      }
    } catch (Exception e) {
      log.warn("Unable to clean up outdated reporting queue '{}'", queueName, e);
    }
  }
}
