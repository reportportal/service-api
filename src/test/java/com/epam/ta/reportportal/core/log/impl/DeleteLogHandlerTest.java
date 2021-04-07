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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteLogHandlerTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private LogRepository logRepository;

	@Mock
	private AttachmentRepository attachmentRepository;

	@Mock
	private TestItemService testItemService;

	@Mock
	private LogIndexer logIndexer;

	@InjectMocks
	private DeleteLogHandlerImpl handler;

	@Test
	void deleteLogOnNotExistProject() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(projectRepository.existsById(projectId)).thenReturn(false);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteLog(1L, extractProjectDetails(user, "test_project"), user)
		);
		assertEquals("Project '1' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void deleteNotExistLog() {
		long projectId = 1L;
		long logId = 2L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(projectRepository.existsById(projectId)).thenReturn(true);
		when(logRepository.findById(logId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteLog(logId, extractProjectDetails(user, "test_project"), user)
		);
		assertEquals("Log '2' not found. Did you use correct Log ID?", exception.getMessage());
	}

	@Test
	void deleteLogByNotOwner() {
		long projectId = 1L;
		long logId = 2L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, projectId);

		Log log = new Log();
		TestItem testItem = new TestItem();
		TestItemResults itemResults = new TestItemResults();
		itemResults.setStatistics(Sets.newHashSet(new Statistics()));
		testItem.setItemResults(itemResults);
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(projectId);
		User user1 = new User();
		user1.setId(1L);
		user1.setLogin("owner");
		launch.setUserId(2L);
		testItem.setLaunchId(launch.getId());
		log.setTestItem(testItem);

		when(testItemService.getEffectiveLaunch(any(TestItem.class))).thenReturn(launch);
		when(projectRepository.existsById(projectId)).thenReturn(true);
		when(logRepository.findById(logId)).thenReturn(Optional.of(log));

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteLog(logId, extractProjectDetails(user, "test_project"), user)
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void cleanUpLogDataTest() {
		long projectId = 1L;
		long logId = 2L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, projectId);

		Log log = new Log();
		TestItem testItem = new TestItem();
		TestItemResults itemResults = new TestItemResults();
		itemResults.setStatistics(Sets.newHashSet(new Statistics()));
		testItem.setItemResults(itemResults);
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(projectId);
		User user1 = new User();
		user1.setId(1L);
		user1.setLogin("owner");
		launch.setUserId(user1.getId());
		testItem.setLaunchId(launch.getId());
		log.setTestItem(testItem);
		Attachment attachment = new Attachment();
		String attachmentPath = "attachmentPath";
		attachment.setFileId(attachmentPath);
		String attachmentThumbnailPath = "attachmentThumbnail";
		attachment.setThumbnailId(attachmentThumbnailPath);
		log.setAttachment(attachment);

		when(testItemService.getEffectiveLaunch(any(TestItem.class))).thenReturn(launch);
		when(projectRepository.existsById(projectId)).thenReturn(true);
		when(logRepository.findById(logId)).thenReturn(Optional.of(log));

		handler.deleteLog(logId, extractProjectDetails(user, "test_project"), user);

		verify(logRepository, times(1)).delete(log);
		verify(logIndexer, times(1)).cleanIndex(projectId, Collections.singletonList(logId));
	}

	@Test
	void cleanUpLogDataNegative() {
		long projectId = 1L;
		long logId = 2L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, projectId);

		Log log = new Log();
		TestItem testItem = new TestItem();
		TestItemResults itemResults = new TestItemResults();
		itemResults.setStatistics(Sets.newHashSet(new Statistics()));
		testItem.setItemResults(itemResults);
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(projectId);
		User user1 = new User();
		user1.setId(1L);
		user1.setLogin("owner");
		launch.setUserId(user1.getId());
		testItem.setLaunchId(launch.getId());
		log.setTestItem(testItem);
		Attachment attachment = new Attachment();
		String attachmentPath = "attachmentPath";
		attachment.setFileId(attachmentPath);
		String attachmentThumbnailPath = "attachmentThumbnail";
		attachment.setThumbnailId(attachmentThumbnailPath);
		log.setAttachment(attachment);
		when(testItemService.getEffectiveLaunch(any(TestItem.class))).thenReturn(launch);
		when(projectRepository.existsById(projectId)).thenReturn(true);
		when(logRepository.findById(logId)).thenReturn(Optional.of(log));
		doThrow(IllegalArgumentException.class).when(logRepository).delete(log);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteLog(logId, extractProjectDetails(user, "test_project"), user)
		);
		assertEquals("Error while Log instance deleting.", exception.getMessage());
	}
}