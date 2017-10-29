package com.epam.ta.reportportal.job;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.epam.ta.reportportal.events.ConsulUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ConsulUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsulUpdateListener.class);

	private final ApplicationEventPublisher eventPublisher;
	private CatalogClient catalogClient;
	private AtomicLong xConsulIndex = new AtomicLong();

	@Autowired
	public ConsulUpdateListener(CatalogClient catalogClient, ApplicationEventPublisher eventPublisher) {
		this.catalogClient = catalogClient;
		this.eventPublisher = eventPublisher;
	}

	@EventListener
	public void onApplicationRefresh(ContextRefreshedEvent event) {
		xConsulIndex.set(catalogClient.getCatalogServices(QueryParams.DEFAULT).getConsulIndex());
		Executors.newSingleThreadExecutor().execute(this::watch);
	}

	private void watch() {
		while (true) {
			try {
				xConsulIndex.set(catalogClient.getCatalogServices(QueryParams.Builder.builder()
						.setIndex(xConsulIndex.get())
						.build()).getConsulIndex());
				eventPublisher.publishEvent(new ConsulUpdateEvent());
			} catch (Exception ignored) {
				LOGGER.info(ignored.getMessage());
			}
		}
	}
}
