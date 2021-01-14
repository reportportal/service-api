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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import com.epam.ta.reportportal.job.service.impl.LaunchCleanerServiceImpl;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LaunchCleanerServiceImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private AttachmentCleanerService attachmentCleanerService;

	@InjectMocks
	private LaunchCleanerServiceImpl launchCleanerService;

	@Test
	void runTest() {
		Project project = new Project();
		project.setId(1L);
		AtomicLong attachmentsRemoved = new AtomicLong();
		AtomicLong thumbnailsRemoved = new AtomicLong();
		ArrayList<Long> launchIds = Lists.newArrayList(1L, 2L, 3L);

		launchIds.forEach(id -> launchCleanerService.cleanLaunch(id, attachmentsRemoved, thumbnailsRemoved));

		verify(launchRepository, times(3)).deleteById(anyLong());
		verify(attachmentCleanerService, times(3)).removeLaunchAttachments(anyLong(), any(), any());
	}
}