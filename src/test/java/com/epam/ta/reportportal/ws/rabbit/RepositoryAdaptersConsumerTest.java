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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class RepositoryAdaptersConsumerTest {

	@Mock
	private LogRepository logRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private DataStoreService dataStoreService;

	@InjectMocks
	private RepositoryAdaptersConsumer repositoryAdaptersConsumer;

	@Test
	void findNotExistProject() {
		String projectName = "test_project";

		when(projectRepository.findByName(projectName)).thenReturn(Optional.empty());

		ProjectResource resource = repositoryAdaptersConsumer.findProjectByName(projectName);

		assertNull(resource);
	}

	@Test
	void findNotExistTestItem() {
		long id = 1L;
		when(testItemRepository.findById(id)).thenReturn(Optional.empty());

		assertNull(repositoryAdaptersConsumer.findTestItem(id));
	}

	@Test
	void findLogsByTestItem() {
		long itemRef = 1L;
		int limit = 3;

		Log log1 = new Log();
		log1.setLogLevel(40000);
		log1.setLogMessage("message");
		log1.setId(1L);
		Log log2 = new Log();
		log2.setId(2L);
		log2.setLogMessage("message");
		log2.setLogLevel(50000);
		when(logRepository.findByTestItemId(itemRef, limit)).thenReturn(Arrays.asList(log1, log2));

		List<LogResource> resources = repositoryAdaptersConsumer.findLogsByTestItem(itemRef, limit, false);

		assertEquals(resources.size(), 2);
	}

	@Test
	void load() {
		String id = "id";
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("data".getBytes(Charset.forName("UTF8")));
		when(dataStoreService.load(id)).thenReturn(byteArrayInputStream);

		assertEquals(byteArrayInputStream, repositoryAdaptersConsumer.fetchData(id));
	}
}