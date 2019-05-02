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
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.configs.rabbit.InternalConfiguration.*;
import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_INTEGRATION_RESOURCE;
import static com.epam.ta.reportportal.ws.rabbit.MessageHeaders.*;

/**
 * @author Pavel Bortnik
 */
@Component
@Transactional
public class RepositoryAdaptersConsumer {

	private IntegrationRepository integrationRepository;

	private LogRepository logRepository;

	private ProjectRepository projectRepository;

	private TestItemRepository testItemRepository;

	private DataStoreService dataStoreService;

	private ProjectConverter projectConverter;

	private TestItemResourceAssembler itemResourceAssembler;

	@Autowired
	public void setIntegrationRepository(IntegrationRepository integrationRepository) {
		this.integrationRepository = integrationRepository;
	}

	@Autowired
	public void setLogRepository(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setDataStoreService(DataStoreService dataStoreService) {
		this.dataStoreService = dataStoreService;
	}

	@RabbitListener(queues = PROJECTS_FIND_BY_NAME)
	public ProjectResource findProjectByName(@Payload String projectName) {
		return projectRepository.findByName(projectName).map(it -> projectConverter.TO_PROJECT_RESOURCE.apply(it)).orElse(null);
	}

	@RabbitListener(queues = INTEGRATION_FIND_ONE)
	public IntegrationResource findIntegrationById(@Payload Long integrationId) {
		return integrationRepository.findById(integrationId).map(TO_INTEGRATION_RESOURCE).orElse(null);
	}

	@RabbitListener(queues = TEST_ITEMS_FIND_ONE_QUEUE)
	public TestItemResource findTestItem(@Payload Long itemId) {
		return testItemRepository.findById(itemId).map(it -> itemResourceAssembler.toResource(it)).orElse(null);
	}

	@RabbitListener(queues = LOGS_FIND_BY_TEST_ITEM_REF_QUEUE)
	public List<LogResource> findLogsByTestItem(@Header(ITEM_REF) Long itemRef,
			@Header(LIMIT) Integer limit,
			@Header(IS_LOAD_BINARY_DATA) boolean loadBinaryData) {
		List<Log> logs = logRepository.findByTestItemId(itemRef, limit /*, loadBinaryData*/);
		return logs.stream().map(LogConverter.TO_RESOURCE).collect(Collectors.toList());
	}

	//TODO think about how to work with such content

	@RabbitListener(queues = DATA_STORAGE_FETCH_DATA_QUEUE)
	public InputStream fetchData(String dataId) {
		return dataStoreService.load(dataId);
	}

}
