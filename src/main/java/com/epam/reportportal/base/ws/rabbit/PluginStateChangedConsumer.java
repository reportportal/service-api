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

package com.epam.reportportal.base.ws.rabbit;

import com.epam.reportportal.base.core.configs.Conditions;
import com.epam.reportportal.base.core.plugin.PluginStateChangedMessage;
import com.epam.reportportal.base.job.CleanOutdatedPluginsJob;
import com.epam.reportportal.base.job.LoadPluginsJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reacts to {@link PluginStateChangedMessage} broadcasts (one per instance, via the {@code broadcast.events} fanout
 * exchange / {@code eventsQueue}) by immediately re-running the same reconciliation the scheduled plugin jobs already
 * perform, instead of waiting for their next poll. The message only carries a plugin id as a wake-up hint; the actual
 * state is always re-read from the database by the jobs themselves, so this handler stays correct regardless of message
 * ordering or duplicate delivery - including the publishing instance receiving its own broadcast back (a cheap no-op,
 * since the change is already applied locally).
 *
 * <p>The scheduled jobs keep running independently as a safety net for missed broadcasts (e.g. a
 * broker reconnect gap).
 */
@Slf4j
@Component
@Conditional(Conditions.NotTestCondition.class)
@RequiredArgsConstructor
public class PluginStateChangedConsumer {

  private final LoadPluginsJob loadPluginsJob;
  private final CleanOutdatedPluginsJob cleanOutdatedPluginsJob;

  @RabbitListener(queues = "#{eventsQueue.name}", containerFactory = "rabbitListenerContainerFactory")
  public void onPluginStateChanged(@Payload PluginStateChangedMessage message) {
    log.debug("Received plugin state change broadcast for plugin id = '{}', reconciling now", message.getPluginId());
    loadPluginsJob.execute();
    cleanOutdatedPluginsJob.execute();
  }

}
