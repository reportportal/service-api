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

package com.epam.ta.reportportal.core.plugin;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.EXCHANGE_PLUGINS;
import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.KEY_PLUGINS_PING;

public class RabbitAwarePluginBox extends AbstractScheduledService implements PluginBox {

	private static final int broadcastTimeout = 5;
	private static final Logger LOGGER = LoggerFactory.getLogger(RabbitAwarePluginBox.class);
	private final Cache<String, Plugin> plugins;

	private final MessageBus messageBus;

	public RabbitAwarePluginBox(MessageBus messageBus) {
		this.messageBus = messageBus;
		this.plugins = CacheBuilder.newBuilder().expireAfterWrite(broadcastTimeout * 2, TimeUnit.SECONDS).build();
	}

	@Override
	public List<Plugin> getPlugins() {
		return ImmutableList.<Plugin>builder().addAll(this.plugins.asMap().values()).build();
	}

	@Override
	public Optional<Plugin> getPlugin(String type) {
		return Optional.ofNullable(this.plugins.getIfPresent(type));
	}

	@Override
	public <T> Optional<T> getInstance(String name, Class<T> type) {
		return Optional.empty();
	}

	@Override
	public <T> Optional<T> getInstance(Class<T> type) {
		//TODO implement
		return Optional.empty();
	}

	@Override
	protected void runOneIteration() {
		try {
			this.messageBus.publish(
					EXCHANGE_PLUGINS,
					KEY_PLUGINS_PING,
					Collections.singletonMap("ok", UUID.randomUUID().toString())
			);
		} catch (Exception e) {
			LOGGER.error("Cannot broadcast ping message to plugins", e);
		}

	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, broadcastTimeout, TimeUnit.SECONDS);
	}

	@RabbitListener(queues = "#{ @pluginsPongQueue.name }")
	void fulfillPluginsList(@Payload Plugin plugin) {
		//		this.plugins.put(plugin.getType(), plugin);
	}

}
