/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.enums.*;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@ExtendWith(MockitoExtension.class)
class LaunchFinishedEventHandlerTest {
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private GetIntegrationHandler getIntegrationHandler;
	@Mock
	private MailServiceFactory mailServiceFactory;
	@Mock
	private UserRepository userRepository;
	@Mock
	private LaunchRepository launchRepository;
	@Mock
	private Provider<HttpServletRequest> currentRequest;
	@Mock
	private AnalyzeCollectorFactory analyzeCollectorFactory;
	@Mock
	private AnalyzerServiceAsync analyzerServiceAsync;
	@Mock
	private LogIndexer logIndexer;

	private Project project = mock(Project.class);

	private Integration emailIntegration = mock(Integration.class);

	private EmailService emailService = mock(EmailService.class);

	private AnalyzeItemsCollector analyzeItemsCollector = mock(AnalyzeItemsCollector.class);

	private HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

	private CompletableFuture<Void> analyze = mock(CompletableFuture.class);

	private Supplier<Set<String>> recipientsSupplier = Suppliers.memoize(this::getRecipients);
	private Supplier<Set<String>> launchNamesSupplier = Suppliers.memoize(this::getLaunchNames);

	@InjectMocks
	private LaunchFinishedEventHandler launchFinishedEventHandler;

	@Test
	void shouldNotSendWhenLaunchInDebug() throws ExecutionException, InterruptedException {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent();
		event.setLaunchActivityResource(resource);
		event.setUserId(1L);

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEBUG);

		when(launchRepository.findById(event.getLaunchActivityResource().getId())).thenReturn(launch);

		launchFinishedEventHandler.onApplicationEvent(event);

		verify(projectRepository, times(0)).findById(launch.get().getId());
	}

	@Test
	void shouldNotSendWhenNotificationsDisabled() throws ExecutionException, InterruptedException {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent();
		event.setLaunchActivityResource(resource);
		event.setUserId(1L);

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		when(launchRepository.findById(event.getLaunchActivityResource().getId())).thenReturn(launch);
		when(projectRepository.findById(resource.getProjectId())).thenReturn(Optional.ofNullable(project));
		when(project.getProjectAttributes()).thenReturn(getProjectAttributesWithDisabledNotifications());
		when(logIndexer.indexLogs(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(2L));
		launchFinishedEventHandler.onApplicationEvent(event);
		verify(logIndexer, times(1)).indexLogs(any(Long.class), anyList(), any(AnalyzerConfig.class));

		verify(getIntegrationHandler, times(0)).getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
				IntegrationGroupEnum.NOTIFICATION
		);

	}

	@Test
	void shouldSendWhenNotificationsEnabled() throws ExecutionException, InterruptedException {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent();
		event.setLaunchActivityResource(resource);
		event.setUserId(1L);

		Optional<Launch> launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

		when(launchRepository.findById(event.getLaunchActivityResource().getId())).thenReturn(launch);
		when(projectRepository.findById(resource.getProjectId())).thenReturn(Optional.ofNullable(project));
		when(project.getProjectAttributes()).thenReturn(getProjectAttributesWithEnabledNotifications());
		when(project.getId()).thenReturn(1L);
		when(getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
				IntegrationGroupEnum.NOTIFICATION
		)).thenReturn(Optional.ofNullable(emailIntegration));

		when(mailServiceFactory.getDefaultEmailService(emailIntegration)).thenReturn(Optional.ofNullable(emailService));

		when(analyzerServiceAsync.hasAnalyzers()).thenReturn(true);

		when(analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)).thenReturn(analyzeItemsCollector);

		when(analyzeItemsCollector.collectItems(any(Long.class), any(Long.class), any(String.class))).thenReturn(Collections.emptyList());

		when(analyzerServiceAsync.analyze(any(Launch.class), anyList(), any(AnalyzerConfig.class))).thenReturn(analyze);

		when(logIndexer.indexLogs(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(2L));

		launchFinishedEventHandler.onApplicationEvent(event);
		verify(logIndexer, times(1)).indexLogs(any(Long.class), anyList(), any(AnalyzerConfig.class));
		verify(analyzerServiceAsync, times(1)).analyze(any(), any(), any());

	}

	@Test
	void shouldSendWhenAutoAnalyzedDisabledEnabled() throws ExecutionException, InterruptedException {

		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(1L);
		resource.setName("name");
		resource.setProjectId(1L);

		LaunchFinishedEvent event = new LaunchFinishedEvent();
		event.setLaunchActivityResource(resource);
		event.setUserId(1L);

		Launch launch = LaunchTestUtil.getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT).get();
		launch.setName("name1");

		when(launchRepository.findById(event.getLaunchActivityResource().getId())).thenReturn(Optional.ofNullable(launch));
		when(projectRepository.findById(resource.getProjectId())).thenReturn(Optional.ofNullable(project));
		when(project.getSenderCases()).thenReturn(getSenderCases());
		when(project.getProjectAttributes()).thenReturn(getProjectAttributesWithEnabledNotificationsAndDisabledAutoAnalyzer());
		when(project.getId()).thenReturn(1L);
		when(getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
				IntegrationGroupEnum.NOTIFICATION
		)).thenReturn(Optional.ofNullable(emailIntegration));

		when(mailServiceFactory.getDefaultEmailService(emailIntegration)).thenReturn(Optional.ofNullable(emailService));
		when(currentRequest.get()).thenReturn(httpServletRequest);
		when(httpServletRequest.getContextPath()).thenReturn("path");
		when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("url"));
		when(httpServletRequest.getHeaderNames()).thenReturn(Collections.enumeration(Lists.newArrayList("authorization")));
		when(httpServletRequest.getHeaders(anyString())).thenReturn(Collections.emptyEnumeration());
		when(logIndexer.indexLogs(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(2L));
		launchFinishedEventHandler.onApplicationEvent(event);
		verify(logIndexer, times(1)).indexLogs(any(Long.class), anyList(), any(AnalyzerConfig.class));
		verify(emailService, times(2)).sendLaunchFinishNotification(any(), any(), any(), any());

	}

	private Set<ProjectAttribute> getProjectAttributesWithDisabledNotifications() {

		Attribute attribute = new Attribute();
		attribute.setName(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute());
		ProjectAttribute projectAttribute = new ProjectAttribute();
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("false");

		return Sets.newHashSet(projectAttribute);
	}

	private Set<ProjectAttribute> getProjectAttributesWithEnabledNotifications() {

		Attribute attribute = new Attribute();
		attribute.setName(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute());
		ProjectAttribute projectAttribute = new ProjectAttribute();
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("true");

		Attribute autoAnalyzed = new Attribute();
		autoAnalyzed.setName(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute());
		ProjectAttribute autoAnalyzedAttribute = new ProjectAttribute();
		autoAnalyzedAttribute.setAttribute(autoAnalyzed);
		autoAnalyzedAttribute.setValue("true");

		return Sets.newHashSet(projectAttribute, autoAnalyzedAttribute);
	}

	private Set<ProjectAttribute> getProjectAttributesWithEnabledNotificationsAndDisabledAutoAnalyzer() {

		Attribute attribute = new Attribute();
		attribute.setName(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute());
		ProjectAttribute projectAttribute = new ProjectAttribute();
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("true");

		Attribute autoAnalyzed = new Attribute();
		autoAnalyzed.setName(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute());
		ProjectAttribute autoAnalyzedAttribute = new ProjectAttribute();
		autoAnalyzedAttribute.setAttribute(autoAnalyzed);
		autoAnalyzedAttribute.setValue("false");

		return Sets.newHashSet(projectAttribute, autoAnalyzedAttribute);
	}

	private Set<SenderCase> getSenderCases() {
		return Arrays.stream(SendCase.values())
				.map(sc -> new SenderCase(recipientsSupplier.get(), launchNamesSupplier.get(), Collections.emptySet(), sc))
				.collect(Collectors.toSet());
	}

	private Set<String> getRecipients() {
		return Sets.newHashSet("first@mail.com", "second@mail.com");
	}

	private Set<String> getLaunchNames() {
		return Sets.newHashSet("name1", "name2");
	}

}