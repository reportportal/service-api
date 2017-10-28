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

@Component
public class ConsulUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsulUpdateListener.class);

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	private CatalogClient catalogClient;

	private Long xConsulIndex;

	@Autowired
	public ConsulUpdateListener(CatalogClient catalogClient, ApplicationEventPublisher eventPublisher) {
		this.catalogClient = catalogClient;
		this.eventPublisher = eventPublisher;
	}

	@EventListener
	public void onApplicationRefresh(ContextRefreshedEvent event) {
		eventPublisher.publishEvent(new ConsulUpdateEvent());
		xConsulIndex = catalogClient.getCatalogServices(QueryParams.DEFAULT).getConsulIndex();
		Executors.newSingleThreadScheduledExecutor().schedule(this::doTheStuff, 3, TimeUnit.MINUTES);
	}

	private void doTheStuff() {
		while (true) {
			LOGGER.info("WAITING FOR CONSUL UPDATING");
			try {
				xConsulIndex = catalogClient.getCatalogServices(QueryParams.Builder.builder()
						.setIndex(xConsulIndex)
						.setWaitTime(3000)
						.build()).getConsulIndex();
				LOGGER.info("CONSUL HAS CHANGED" + xConsulIndex);
				eventPublisher.publishEvent(new ConsulUpdateEvent());
			} catch (Exception ignored) {
				System.out.println("exception");
				//ignore
			}
		}
	}

}
