package com.epam.ta.reportportal.core.plugin;

import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RabbitAwarePluginBox extends AbstractScheduledService implements PluginBox {

	private final Map<String, Plugin> plugins;
	private final AmqpTemplate amqpTemplate;
	private static final int broadcastTimeout = 5;

	public RabbitAwarePluginBox(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
		this.plugins = new ConcurrentHashMap<>();
	}

	@Override
	public List<Plugin> getPlugins() {
		return ImmutableList.<Plugin>builder().addAll(this.plugins.values()).build();
	}

	@Override
	public Optional<Plugin> getPlugin(String type) {
		return Optional.ofNullable(this.plugins.get(type));
	}

	@Override
	protected void runOneIteration() {
		try {
			this.amqpTemplate.convertAndSend(RabbitMqConfiguration.EXCHANGE_PLUGINS,
					RabbitMqConfiguration.KEY_PLUGINS_PING,
					Collections.singletonMap("ok", UUID.randomUUID().toString())
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, broadcastTimeout, TimeUnit.SECONDS);
	}

	@RabbitListener(queues = "#{ @pluginsPongQueue.name }")
	void fulfillPluginsList(@Payload Plugin plugin) {
		this.plugins.replace(plugin.getType(), plugin);
	}

	/*
		EXAMPLE OF RECEIVER
	 */
	@RabbitListener(queues = "#{ @pluginsPingQueue.name }")
	void fulfillPluginsList2(@Payload Map<String, ?> payload) {
		System.out.println("PONG2 IS THERE! " + payload);
		this.amqpTemplate.convertAndSend(RabbitMqConfiguration.EXCHANGE_PLUGINS,
				RabbitMqConfiguration.KEY_PLUGINS_PONG,
				new Plugin(UUID.randomUUID().toString())
		);
	}

}
