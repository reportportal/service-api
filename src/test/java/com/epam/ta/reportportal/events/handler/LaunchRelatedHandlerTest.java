/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.ProjectSettingsRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.events.LaunchFinishedEvent;
import com.epam.ta.reportportal.util.email.EmailService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration("classpath:report-portal-ws-servlet.xml")
// @ActiveProfiles({ "unittest", "epam" })
// @SpringFixture("notifications")
// TODO: tests here look very similar to each other, and it makes much sense to
// use parameters for them. But using parameters with rules is tricky. Need to
// implement proper parameters handling.
@Ignore
public class LaunchRelatedHandlerTest {
	@Autowired
	@Mock
	EmailService service;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectSettingsRepository settingsRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	@InjectMocks
	LaunchActivityHandler launchRelatedAspect;

	HttpServletRequest httpServletRequest;

	@Before
	public void init() throws Throwable {
		MockitoAnnotations.initMocks(this);
		httpServletRequest = new MockHttpServletRequest(HttpMethod.PUT.name(), "https://localhost:8443");
	}

	@Test
	public void alwaysSendCaseFailedLaunch() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_always_send");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824cd1783fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void alwaysSendCasePassedLaunch() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_always_send");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51856cd1776fh743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void failedSendCaseFailedLaunch() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_failed");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824cd1783fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void failedSendCasePassedLaunch() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_failed");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51856cd1776fh743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service, times(0)).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(),
				eq(launch), anyString(), settings);
	}

	@Test
	public void more10SendCasePositive() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_more_10");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824cd1783fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void more20SendCasePositive() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_more_20");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824cd1783fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void more10SendCaseNegative() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_more_10");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51856cd1776fh743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service, times(0)).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(),
				eq(launch), anyString(), settings);
	}

	@Test
	public void more20SendCaseNegative() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_more_20");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824fr2343fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service, times(0)).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(),
				eq(launch), anyString(), settings);
	}

	@Test
	public void more50SendCasePositive() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_more_50");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51828fr2343fg743b3e6g42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void more50SendCaseNegative() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_more_50");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824cd1783fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service, times(0)).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(),
				eq(launch), anyString(), settings);
	}

	@Test
	public void launchNamesPositive() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_launches");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51824fr2343fg743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(), eq(launch),
				anyString(), settings);
	}

	@Test
	public void launchNamesNegative() throws Throwable {
		Project project = projectRepository.findByName("project_ntf_launches");
		List<String> recipients = project.getConfiguration().getEmailConfig().getEmailCases().iterator().next().getRecipients();
		Launch launch = launchRepository.findOne("51856cd1776fh743b3e5a42c");
		ProjectSettings settings = settingsRepository.findOne(launch.getProjectRef());
		LaunchFinishedEvent launchFinishedEvent = new LaunchFinishedEvent(launch, project);
		launchRelatedAspect.onLaunchFinish(launchFinishedEvent);
		verify(service, times(0)).sendLaunchFinishNotification(eq(recipients.toArray(new String[recipients.size()])), anyString(),
				eq(launch), anyString(), settings);
	}
}