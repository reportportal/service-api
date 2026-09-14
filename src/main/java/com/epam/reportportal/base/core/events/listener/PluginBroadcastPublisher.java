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

package com.epam.reportportal.base.core.events.listener;

import com.epam.reportportal.base.core.configs.rabbit.InternalConfiguration;
import com.epam.reportportal.base.core.events.MessageBus;
import com.epam.reportportal.base.core.plugin.PluginStateChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes {@link PluginStateChangedMessage} to the {@code broadcast.events} fanout exchange (one anonymous,
 * auto-deleted queue per instance) after the enclosing transaction commits - mirroring {@link DomainEventPublisher}'s
 * after-commit pattern for the same reason: publishing mid-transaction would let another instance react to a plugin
 * change before this instance's own database write is visible to it.
 *
 * <p>{@code fallbackExecution = true} also fires the publishing immediately when there is no active
 * transaction (e.g. a call site outside a {@code @Transactional} boundary).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginBroadcastPublisher {

  private final MessageBus messageBus;

  @Async(value = "eventListenerExecutor")
  @TransactionalEventListener(fallbackExecution = true)
  public void onPluginStateChanged(PluginStateChangedMessage message) {
    log.debug("Broadcasting plugin state change for plugin id = '{}'", message.getPluginId());
    messageBus.publish(InternalConfiguration.EXCHANGE_EVENTS, InternalConfiguration.KEY_EVENTS, message);
  }

}
