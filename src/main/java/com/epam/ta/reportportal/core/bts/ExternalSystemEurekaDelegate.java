/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.core.bts;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Delegates executions to microservices registered with Eureka
 *
 * @author Andrei Varabyeu
 */
public class ExternalSystemEurekaDelegate implements ExternalSystemStrategy {

	@Autowired
	private DiscoveryClient discoveryClient;

	private final RestTemplate restTemplate;

	public ExternalSystemEurekaDelegate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	void checkAvailable(String systemType) {
		getServiceInstance(systemType);
	}

	@Override
	public boolean connectionTest(BugTrackingSystem system) {
		return restTemplate.postForObject(getServiceInstance(system.getBtsType()).getUri().toString() + "/check", system, YesNoRS.class,
				system.getId()
		).getIs();
	}

	@Override
	public Optional<Ticket> getTicket(String id, BugTrackingSystem system) {
		return Optional.ofNullable(
				restTemplate.getForObject(getServiceInstance(system.getBtsType()).getUri().toString() + "/{systemId}/ticket/{id}",
						Ticket.class, system.getId(), id
				));
	}

	@Override
	public Ticket submitTicket(PostTicketRQ ticketRQ, BugTrackingSystem system) {
		return restTemplate.postForObject(getServiceInstance(system.getBtsType()).getUri().toString() + "/{systemId}/ticket", ticketRQ,
				Ticket.class, system.getId()
		);

	}

	@Override
	public List<PostFormField> getTicketFields(String issueType, BugTrackingSystem system) {
		return restTemplate.exchange(getServiceInstance(system.getBtsType()).getUri().toString() + "/{systemId}/ticket/{issueType}/fields",
				HttpMethod.GET, null, new ParameterizedTypeReference<List<PostFormField>>() {
				}, system.getId(), issueType
		).getBody();
	}

	@Override
	public List<String> getIssueTypes(BugTrackingSystem system) {
		return restTemplate.exchange(getServiceInstance(system.getBtsType()).getUri().toString() + "/{systemId}/ticket/types",
				HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
				}, system.getId()
		).getBody();
	}

	private ServiceInstance getServiceInstance(String externalSystem) {
		String externalSystemType = externalSystem.toLowerCase();

		Optional<ServiceInstance> delegate = discoveryClient.getServices()
				.stream()
				.flatMap(service -> discoveryClient.getInstances(service).stream())
				.filter(instance -> externalSystemType.equals(instance.getMetadata().get("extension")))
				.findAny();

		BusinessRule.expect(delegate, Preconditions.IS_PRESENT)
				.verify(ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
						"External system with type " + externalSystem + " is not deployed or not available"
				);
		return delegate.get();
	}
}