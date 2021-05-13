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

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.events.activity.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.activity.IssueTypeActivityResource;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DefectTypeDeletedHandlerTest {

	@Mock
	private AnalyzerStatusCache analyzerStatusCache;

	@Mock
	private AnalyzerServiceClient analyzerServiceClient;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private LogIndexer logIndexer;

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private DefectTypeDeletedHandler handler;

	@Test
	void deleteSubTypeOnNotExistProject() {
		long projectId = 2L;

		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.handleDefectTypeDeleted(new DefectTypeDeletedEvent(new IssueTypeActivityResource(), 1L, "user", projectId))
		);

		assertEquals("Project '2' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void noClientsTest() {
		long projectId = 2L;

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
		when(analyzerServiceClient.hasClients()).thenReturn(false);

		handler.handleDefectTypeDeleted(new DefectTypeDeletedEvent(new IssueTypeActivityResource(), 1L, "user", projectId));

		verifyZeroInteractions(logIndexer);
	}

	@Test
	void analysisAlreadyRunningTest() {
		long projectId = 2L;

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		Cache<Long, Long> cache = CacheBuilder.newBuilder().build();
		cache.put(2L, projectId);
		when(analyzerStatusCache.getAnalyzeStatus(AnalyzerStatusCache.AUTO_ANALYZER_KEY)).thenReturn(Optional.of(cache));

		ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.handleDefectTypeDeleted(new DefectTypeDeletedEvent(new IssueTypeActivityResource(), 1L, "user", projectId))
		);
		assertEquals("Forbidden operation. Index can not be removed until auto-analysis proceeds.", exception.getMessage());
	}

	@Test
	void successfullyReindex() {
		long projectId = 2L;

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(getProjectWithAnalyzerAttributes(projectId)));
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		when(analyzerStatusCache.getAnalyzeStatus(AnalyzerStatusCache.AUTO_ANALYZER_KEY)).thenReturn(Optional.of(CacheBuilder.newBuilder().build()));
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L);

		handler.handleDefectTypeDeleted(new DefectTypeDeletedEvent(new IssueTypeActivityResource(), 1L, "user", projectId));

		verify(logIndexer, times(1)).index(eq(projectId), any(AnalyzerConfig.class));
	}

	private Project getProjectWithAnalyzerAttributes(Long projectId) {
		Project project = new Project();
		project.setProjectAttributes(Sets.newHashSet(
				getProjectAttribute(project, getAttribute("analyzer.isAutoAnalyzerEnabled"), "false"),
				getProjectAttribute(project, getAttribute("analyzer.minDocFreq"), "7"),
				getProjectAttribute(project, getAttribute("analyzer.minTermFreq"), "2"),
				getProjectAttribute(project, getAttribute("analyzer.minShouldMatch"), "80"),
				getProjectAttribute(project, getAttribute("analyzer.numberOfLogLines"), "5"),
				getProjectAttribute(project, getAttribute("analyzer.indexingRunning"), "false")
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