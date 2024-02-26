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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteProjectHandlerImplTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private LogIndexer logIndexer;

	@Mock
	private AnalyzerServiceClient analyzerServiceClient;

	@Mock
	private AnalyzerStatusCache analyzerStatusCache;

	@Mock
	private MessageBus messageBus;

	@Mock
	private IssueTypeRepository issueTypeRepository;

	@Mock
	private ContentRemover<Project> projectContentRemover;

	@Mock
	private LogRepository logRepository;

	@Mock
	private AttachmentBinaryDataService attachmentBinaryDataService;

	@InjectMocks
	private DeleteProjectHandlerImpl handler;

	@Test
	void deleteNotExistProject() {
		Long projectId = 1L;
    ReportPortalUser user =
        getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

    ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> handler.deleteProject(projectId, user));

		assertEquals("Project '1' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void deleteIndexOnNotExistProject() {
		String projectKey = "notExist";
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deleteProjectIndex(projectKey, "user"));

		assertEquals("Project 'notExist' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void deleteProjectIndexByNotExistUser() {
		String projectKey = "notExist";
		String userName = "user";
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(new Project()));
		when(userRepository.findByLogin(userName)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deleteProjectIndex(projectKey, "user"));

		assertEquals("User 'user' not found.", exception.getMessage());
	}

	@Test
	void deleteIndexWhenIndexingRunning() {
		String userName = "user";
		Long projectId = 1L;
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.of(getProjectWithAnalyzerAttributes(projectId, true)));
		when(userRepository.findByLogin(userName)).thenReturn(Optional.of(new User()));

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deleteProjectIndex(TEST_PROJECT_KEY, "user"));

		assertEquals("Forbidden operation. Index can not be removed until index generation proceeds.", exception.getMessage());
	}

	@Test
	void deleteIndexWhenIndexingCacheNotInvalidated() {
		String projectKey = TEST_PROJECT_KEY;
		String userName = "user";
		Long projectId = 1L;
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(getProjectWithAnalyzerAttributes(projectId, false)));
		when(userRepository.findByLogin(userName)).thenReturn(Optional.of(new User()));
		Cache<Long, Long> cache = CacheBuilder.newBuilder().build();
		cache.put(2L, projectId);
		when(analyzerStatusCache.getAnalyzeStatus(AnalyzerStatusCache.AUTO_ANALYZER_KEY)).thenReturn(Optional.of(cache));

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deleteProjectIndex(projectKey, "user"));

		assertEquals("Forbidden operation. Index can not be removed until index generation proceeds.", exception.getMessage());
	}

	@Test
	void deleteIndexWhenThereAreNoAnalyzers() {
		String projectName = TEST_PROJECT_KEY;
		when(analyzerServiceClient.hasClients()).thenReturn(false);

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.deleteProjectIndex(projectName, "user"));

		assertEquals("Impossible interact with integration. There are no analyzer deployed.", exception.getMessage());
	}

	@Test
	void happyDeleteIndex() {
		String userName = "user";
		Long projectId = 1L;
		Project project = getProjectWithAnalyzerAttributes(projectId, false);
		project.setName(TEST_PROJECT_KEY);
		when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.of(project));
		when(userRepository.findByLogin(userName)).thenReturn(Optional.of(new User()));
		when(analyzerStatusCache.getAnalyzeStatus(AnalyzerStatusCache.AUTO_ANALYZER_KEY)).thenReturn(Optional.of(CacheBuilder.newBuilder()
				.build()));
		when(analyzerServiceClient.hasClients()).thenReturn(true);

		OperationCompletionRS response = handler.deleteProjectIndex(TEST_PROJECT_KEY, "user");

		verify(logIndexer, times(1)).deleteIndex(projectId);
		verify(messageBus, times(1)).publishActivity(any(ProjectIndexEvent.class));

		assertEquals(response.getResultMessage(), "Project index with key = '" + TEST_PROJECT_KEY + "' is successfully deleted.");

	}

	@Test
	void deleteProjectTest() {
		String projectName = TEST_PROJECT_KEY;
		Long projectId = 1L;
		Project project = getProjectWithAnalyzerAttributes(projectId, false);
		project.setName(projectName);
    ReportPortalUser user =
        getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		when(issueTypeRepository.getDefaultIssueTypes()).thenReturn(new ArrayList<>());
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(logRepository.deleteByProjectId(1L)).thenReturn(10);

    OperationCompletionRS response = handler.deleteProject(1L, user);

		verify(projectContentRemover, times(1)).remove(project);
		verify(logIndexer, times(1)).deleteIndex(projectId);
		verify(analyzerServiceClient, times(1)).removeSuggest(projectId);
		verify(projectContentRemover, times(1)).remove(any(Project.class));

		assertEquals(response.getResultMessage(), "Project with id = '" + project.getId() + "' has been successfully deleted.");

	}

	private Project getProjectWithAnalyzerAttributes(Long projectId, boolean indexingRunning) {
		Project project = new Project();
		project.setProjectAttributes(Sets.newHashSet(
				getProjectAttribute(project, getAttribute("analyzer.isAutoAnalyzerEnabled"), "false"),
				getProjectAttribute(project, getAttribute("analyzer.minDocFreq"), "7"),
				getProjectAttribute(project, getAttribute("analyzer.minTermFreq"), "2"),
				getProjectAttribute(project, getAttribute("analyzer.minShouldMatch"), "80"),
				getProjectAttribute(project, getAttribute("analyzer.numberOfLogLines"), "5"),
				getProjectAttribute(project, getAttribute("analyzer.indexingRunning"), String.valueOf(indexingRunning))
		));
		project.setId(projectId);
		return project;
	}

	private ProjectAttribute getProjectAttribute(Project project, Attribute attribute, String value) {
		return new ProjectAttribute().withProject(project).withAttribute(attribute).withValue(value);
	}

	private Attribute getAttribute(String name) {
		Attribute attribute = new Attribute();
		attribute.setName(name);
		return attribute;
	}
}
