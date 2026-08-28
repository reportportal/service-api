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

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Owns the lifecycle of the reporting listener containers.
 *
 * <p>Consuming starts only after the application context has been refreshed. On shutdown the
 * queues of this instance are first unbound so that the exchange stops routing to a dying instance,
 * then the consumers are given a chance to drain what is left before they are stopped and the
 * queues are released.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
public class ReportingListenersLifecycle implements SmartLifecycle {

  /**
   * Matches the default phase of a listener container, so reporting consumers stop together with
   * the rest of the messaging infrastructure and well before the singletons they depend on are
   * destroyed.
   */
  private static final int PHASE = Integer.MAX_VALUE - 1000;

  private static final long DRAIN_POLL_INTERVAL_MS = 200L;

  private final List<AbstractMessageListenerContainer> containers;
  private final List<Binding> bindings;
  private final List<String> queues;
  private final AmqpAdmin amqpAdmin;
  private final ReportingShovelService shovelService;
  private final Duration drainTimeout;

  private volatile boolean running;

  public ReportingListenersLifecycle(
      @Qualifier("listenerContainers") List<AbstractMessageListenerContainer> containers,
      @Qualifier("reportingBindings") List<Binding> bindings,
      @Qualifier("reportingQueues") List<Queue> queues, AmqpAdmin amqpAdmin,
      ReportingShovelService shovelService,
      @Value("${reporting.shutdown.drain-timeout:PT10S}") Duration drainTimeout) {
    this.containers = containers;
    this.bindings = bindings;
    this.queues = queues.stream().map(Queue::getName).collect(Collectors.toList());
    this.amqpAdmin = amqpAdmin;
    this.shovelService = shovelService;
    this.drainTimeout = drainTimeout;
  }

  @Override
  public void start() {
    containers.forEach(AbstractMessageListenerContainer::start);
    running = true;
    log.info("Started {} reporting listener containers", containers.size());
  }

  @Override
  public void stop() {
    if (!running) {
      return;
    }
    running = false;
    Set<String> queuesWithStoppedRouting = stopRouting();
    awaitDrain();
    stopContainers();
    releaseQueues(queuesWithStoppedRouting);
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return PHASE;
  }

  private Set<String> stopRouting() {
    Set<String> queuesWithStoppedRouting = new HashSet<>(queues);
    bindings.forEach(binding -> {
      try {
        amqpAdmin.removeBinding(binding);
      } catch (Exception e) {
        queuesWithStoppedRouting.remove(binding.getDestination());
        log.warn("Unable to unbind reporting queue '{}' from '{}'", binding.getDestination(),
            binding.getExchange(), e);
      }
    });
    return queuesWithStoppedRouting;
  }

  private void awaitDrain() {
    long deadline = System.nanoTime() + drainTimeout.toNanos();
    long pending = readyMessages();
    while (pending > 0 && System.nanoTime() < deadline && sleep()) {
      pending = readyMessages();
    }
    if (pending > 0) {
      log.warn("{} reporting messages left in the queues of this instance after a {} drain timeout",
          pending, drainTimeout);
    }
  }

  private void stopContainers() {
    containers.forEach(container -> {
      try {
        container.stop();
      } catch (Exception e) {
        log.warn("Unable to stop reporting listener container '{}'", container.getListenerId(), e);
      }
    });
  }

  private void releaseQueues(Set<String> queuesWithStoppedRouting) {
    queues.forEach(queue -> {
      try {
        if (queuesWithStoppedRouting.contains(queue)
            && (readyMessages(queue) > 0 || !deleteQueue(queue))) {
          shovelService.republishToReportingExchange(queue);
        }
      } catch (Exception e) {
        log.warn("Unable to release reporting queue '{}' on shutdown", queue, e);
      }
    });
  }

  private boolean deleteQueue(String queue) {
    try {
      shovelService.removeShovel(queue);
      amqpAdmin.deleteQueue(queue, true, true);
      return true;
    } catch (Exception e) {
      log.debug("Reporting queue '{}' cannot be deleted yet", queue, e);
      return false;
    }
  }

  private long readyMessages() {
    return queues.stream().mapToLong(this::readyMessages).sum();
  }

  private long readyMessages(String queue) {
    try {
      QueueInformation info = amqpAdmin.getQueueInfo(queue);
      return info == null ? 0 : info.getMessageCount();
    } catch (Exception e) {
      log.debug("Unable to read the message count of reporting queue '{}'", queue, e);
      return 0;
    }
  }

  private boolean sleep() {
    try {
      Thread.sleep(DRAIN_POLL_INTERVAL_MS);
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
