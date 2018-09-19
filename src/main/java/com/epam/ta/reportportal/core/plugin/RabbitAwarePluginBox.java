package com.epam.ta.reportportal.core.plugin;

import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
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
	protected void runOneIteration() {
		try {
			this.messageBus.publish(
					RabbitMqConfiguration.EXCHANGE_PLUGINS,
					RabbitMqConfiguration.KEY_PLUGINS_PING,
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
		this.plugins.put(plugin.getType(), plugin);
	}

}
