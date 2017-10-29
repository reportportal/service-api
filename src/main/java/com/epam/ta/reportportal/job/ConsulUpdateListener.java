/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * Listens if any services updates are made in consul
 *
 * @author Pavel Bortnik
 */
@Component
public class ConsulUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsulUpdateListener.class);
	private static final int TIMEOUT_IN_SEC = 50;

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
		try {
			xConsulIndex.set(catalogClient.getCatalogServices(QueryParams.DEFAULT).getConsulIndex());
			Executors.newSingleThreadExecutor().execute(this::watch);
		} catch (Exception e) {
			LOGGER.error("Problem with connection to consul.", e);
		}
	}

	private void watch() {
		try {
			while (true) {
				xConsulIndex.set(catalogClient.getCatalogServices(QueryParams.Builder.builder()
						.setIndex(xConsulIndex.get())
						.setWaitTime(TIMEOUT_IN_SEC)
						.build()).getConsulIndex());
				eventPublisher.publishEvent(new ConsulUpdateEvent());
			}
		} catch (Exception ex) {
			LOGGER.error("Problem with connection to consul. ", ex);
		}
	}
}
