package com.epam.ta.reportportal.ws.rabbit;

import com.epam.reportportal.extension.constants.RabbitConstants;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.store.service.DataStoreService;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
@Component
public class RepositoryAdaptersConsumer {

	private IntegrationRepository integrationRepository;

	private LogRepository logRepository;

	private ProjectRepository projectRepository;

	private TestItemRepository testItemRepository;

	private DataStoreService dataStoreService;

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

	@RabbitListener(queues = RabbitConstants.QueueNames.PROJECTS_FIND_BY_NAME)
	public ProjectResource findProjectByName(@Payload String projectName) {
		Project project = projectRepository.findByName(projectName).orElse(null);
		if (null != project) {
			return ProjectConverter.TO_PROJECT_RESOURCE.apply(project);
		}
		return null;
	}

	@RabbitListener(queues = RabbitConstants.QueueNames.INTEGRATION_FIND_ONE)
	public IntegrationResource findIntegrationById(@Payload Long integrationId) {
		Integration integration = integrationRepository.findById(integrationId).orElse(null);
		if (null != integration) {
			return IntegrationConverter.TO_INTEGRATION_RESOURCE.apply(integration);
		}
		return null;
	}

	@RabbitListener(queues = RabbitConstants.QueueNames.TEST_ITEMS_FIND_ONE_QUEUE)
	public TestItemResource findTestItem(@Payload Long itemId) {
		TestItem testItem = testItemRepository.findById(itemId).orElse(null);
		if (testItem != null) {
			return TestItemConverter.TO_RESOURCE.apply(testItem.getItemStructure());
		}
		return null;
	}

	@RabbitListener(queues = RabbitConstants.QueueNames.LOGS_FIND_BY_TEST_ITEM_REF_QUEUE)
	public List<LogResource> findLogsByTestItem(@Header(RabbitConstants.MessageHeaders.ITEM_REF) Long itemRef,
			@Header(RabbitConstants.MessageHeaders.LIMIT) Integer limit,
			@Header(RabbitConstants.MessageHeaders.IS_LOAD_BINARY_DATA) boolean loadBinaryData) {
		List<Log> logs = logRepository.findByTestItemId(itemRef, limit, loadBinaryData);
		return logs.stream().map(LogConverter.TO_RESOURCE).collect(Collectors.toList());
	}

	//TODO think about how to work with such content

	@RabbitListener(queues = RabbitConstants.QueueNames.DATA_STORAGE_FETCH_DATA_QUEUE)
	public InputStream fetchData(String dataId) {
		InputStream load = dataStoreService.load(dataId);
		return load;
	}

}
