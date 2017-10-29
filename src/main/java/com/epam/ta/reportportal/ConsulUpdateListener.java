package com.epam.ta.reportportal;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.catalog.CatalogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ConsulUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsulUpdateListener.class);

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	private CatalogClient catalogClient;

	private AtomicLong xConsulIndex = new AtomicLong();

	@Autowired
	public ConsulUpdateListener(CatalogClient catalogClient, ApplicationEventPublisher eventPublisher) {
		this.catalogClient = catalogClient;
		this.eventPublisher = eventPublisher;
	}

	@EventListener
	public void onApplicationRefresh(ContextRefreshedEvent event) {
		eventPublisher.publishEvent(new ConsulUpdateEvent());
		xConsulIndex.set(catalogClient.getCatalogServices(QueryParams.DEFAULT).getConsulIndex());
		Executors.newSingleThreadExecutor().execute(this::watch);

	}

	private void watch() {
		try {
			xConsulIndex.set(catalogClient.getCatalogServices(QueryParams.Builder.builder()
					.setIndex(xConsulIndex.get())
					.setWaitTime(TimeUnit.MINUTES.toMillis(3))
					.build()).getConsulIndex());
			eventPublisher.publishEvent(new ConsulUpdateEvent());
			watch();
		} catch (Exception ignored) {
			LOGGER.info(ignored.getMessage());
			watch();
		}
	}
}
