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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Listens if any services updates are made in consul
 *
 * @author Pavel Bortnik
 */
@Component
@Profile("!unittest")
public class ConsulUpdateListener extends AbstractExecutionThreadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsulUpdateListener.class);
	private static final int TIMEOUT_IN_SEC = 50;

	private ApplicationEventPublisher eventPublisher;
	private CatalogClient catalogClient;
	private long xConsulIndex;

	@Autowired
	public ConsulUpdateListener(CatalogClient catalogClient, ApplicationEventPublisher eventPublisher) {
		this.catalogClient = catalogClient;
		this.eventPublisher = eventPublisher;
	}

	@EventListener
	public void onApplicationReady(ApplicationReadyEvent event) {
		try {
			this.startAsync().awaitRunning(5, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
			throw new ReportPortalException("Cannot start consul listener.", e);
		}
	}

	@Override
	protected void run() {
		while (isRunning()) {
			try {
				xConsulIndex = catalogClient.getCatalogServices(
						QueryParams.Builder.builder().setIndex(xConsulIndex).setWaitTime(TIMEOUT_IN_SEC).build()).getConsulIndex();
				eventPublisher.publishEvent(new ConsulUpdateEvent());
			} catch (Exception e) {
				xConsulIndex = 0;
				LOGGER.error("Problem interacting with consul. Trying again.", e);
			}
		}
	}
}
