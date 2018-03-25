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

package com.epam.ta.reportportal.core.externalsystem.handler.impl;

import com.epam.ta.reportportal.core.externalsystem.ExternalSystemStrategy;
import com.epam.ta.reportportal.core.externalsystem.StrategyProvider;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.events.TicketPostedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Pavel Bortnik
 */
public class CreateTicketHandlerTest {

	private static final String EXTERNAL_SYSTEM_ID = "externalSystemId";

	private static final String PROJECT_ID = "projectId";

	@Mock
	private StrategyProvider strategyProvider;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private ExternalSystemRepository externalSystemRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	public CreateTicketHandler createTicketHandler;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void injectMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInvalidRq() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setIsIncludeLogs(true);
		postTicketRQ.setBackLinks(null);
		exception.expect(ReportPortalException.class);
		exception.expectMessage(
				"Impossible post ticket to external system. Test item id should be specified, when logs required in ticket description.");
		createTicketHandler.createIssue(postTicketRQ, "project", "id", "user");
	}

	@Test
	public void testInvalidRq2() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setIsIncludeScreenshots(true);
		postTicketRQ.setBackLinks(null);
		exception.expect(ReportPortalException.class);
		exception.expectMessage(
				"Impossible post ticket to external system. Test item id should be specified, when logs required in ticket description.");
		createTicketHandler.createIssue(postTicketRQ, "project", "id", "user");
	}

	@Test
	public void projectNull() {
		PostTicketRQ postTicketRQ = postTicketRq();
		when(testItemRepository.findByIds(
				postTicketRQ.getBackLinks().keySet(), ImmutableList.<String>builder().add("_id").add("name").build())).thenReturn(items());
		when(projectRepository.findByName(PROJECT_ID)).thenReturn(null);
		exception.expect(ReportPortalException.class);
		exception.expectMessage("Project '" + PROJECT_ID + "' not found. Did you use correct project name?");
		createTicketHandler.createIssue(postTicketRQ, PROJECT_ID, EXTERNAL_SYSTEM_ID, "user");
	}

	@Test
	public void projectNotConfigured() {
		PostTicketRQ postTicketRQ = postTicketRq();
		when(testItemRepository.findByIds(
				postTicketRQ.getBackLinks().keySet(), ImmutableList.<String>builder().add("_id").add("name").build())).thenReturn(items());
		Project project = project();
		project.getConfiguration().setExternalSystem(null);
		when(projectRepository.findByName(PROJECT_ID)).thenReturn(project);
		exception.expect(ReportPortalException.class);
		exception.expectMessage("Project '" + PROJECT_ID + "' not configured with ExternalSystems.");
		createTicketHandler.createIssue(postTicketRQ, PROJECT_ID, EXTERNAL_SYSTEM_ID, "user");
	}

	@Test
	public void externalSystemNull() {
		PostTicketRQ postTicketRQ = postTicketRq();
		when(testItemRepository.findByIds(
				postTicketRQ.getBackLinks().keySet(), ImmutableList.<String>builder().add("_id").add("name").build())).thenReturn(items());
		when(projectRepository.findByName(PROJECT_ID)).thenReturn(project());
		when(externalSystemRepository.findOne(EXTERNAL_SYSTEM_ID)).thenReturn(null);
		exception.expect(ReportPortalException.class);
		exception.expectMessage("ExternalSystem with ID '" + EXTERNAL_SYSTEM_ID + "' not found. Did you use correct ExternalSystem ID?");
		createTicketHandler.createIssue(postTicketRQ, PROJECT_ID, EXTERNAL_SYSTEM_ID, "user");
	}

	@Test
	public void fieldsNull() {
		PostTicketRQ postTicketRQ = postTicketRq();
		when(testItemRepository.findByIds(
				postTicketRQ.getBackLinks().keySet(), ImmutableList.<String>builder().add("_id").add("name").build())).thenReturn(items());
		when(projectRepository.findByName(PROJECT_ID)).thenReturn(project());
		ExternalSystem externalSystem = externalSystem();
		externalSystem.setFields(null);
		when(externalSystemRepository.findOne(EXTERNAL_SYSTEM_ID)).thenReturn(externalSystem);
		exception.expect(ReportPortalException.class);
		exception.expectMessage("There aren't any submitted BTS fields!");
		createTicketHandler.createIssue(postTicketRQ, PROJECT_ID, EXTERNAL_SYSTEM_ID, "user");
	}

	@Test
	public void testCreateTicket() {
		PostTicketRQ postTicketRQ = postTicketRq();
		when(testItemRepository.findByIds(
				postTicketRQ.getBackLinks().keySet(), ImmutableList.<String>builder().add("_id").add("name").build())).thenReturn(items());
		when(projectRepository.findByName(PROJECT_ID)).thenReturn(project());
		ExternalSystem externalSystem = externalSystem();
		when(externalSystemRepository.findOne(EXTERNAL_SYSTEM_ID)).thenReturn(externalSystem);
		ExternalSystemStrategy strategy = mock(ExternalSystemStrategy.class);
		when(strategyProvider.getStrategy(externalSystem.getExternalSystemType())).thenReturn(strategy);
		when(strategy.submitTicket(postTicketRQ, externalSystem)).thenReturn(ticket());

		createTicketHandler.createIssue(postTicketRQ, PROJECT_ID, EXTERNAL_SYSTEM_ID, "user");
		verify(eventPublisher, times(2)).publishEvent(any(TicketPostedEvent.class));
	}

	private Ticket ticket() {
		Ticket ticket = new Ticket();
		ticket.setId("ticketID");
		return ticket;
	}

	private PostTicketRQ postTicketRq() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setIsIncludeLogs(true);
		postTicketRQ.setIsIncludeScreenshots(true);
		postTicketRQ.setIsIncludeComments(true);
		postTicketRQ.setNumberOfLogs(2);
		postTicketRQ.setBackLinks(ImmutableMap.<String, String>builder().put("id1", "link1").put("id2", "link2").build());
		return postTicketRQ;
	}

	private List<TestItem> items() {
		List<TestItem> items = new ArrayList<>();
		TestItem testItem = new TestItem();
		testItem.setId("id1");
		testItem.setName("testItem1");
		items.add(testItem);
		testItem = new TestItem();
		testItem.setId("id2");
		testItem.setName("testItem2");
		items.add(testItem);
		return items;
	}

	private Project project() {
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setExternalSystem(Collections.singletonList(EXTERNAL_SYSTEM_ID));
		project.setConfiguration(configuration);
		return project;
	}

	private ExternalSystem externalSystem() {
		ExternalSystem externalSystem = new ExternalSystem();
		externalSystem.setId(EXTERNAL_SYSTEM_ID);
		externalSystem.setExternalSystemType("JIRA");
		externalSystem.setFields(Collections.emptyList());
		return externalSystem;
	}

}