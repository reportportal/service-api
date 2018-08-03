package com.epam.ta.reportportal.ws.rabbit;

import com.epam.reportportal.extension.constants.RabbitConstants;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.store.service.DataStoreService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

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

	@RabbitListener(queues = RabbitConstants.QueueNames.PROJECTS_FIND_BY_NAME)
	public Project findProjectByName(@Payload String projectName) {
		return projectRepository.findByName(projectName).orElse(null);
	}

	@RabbitListener(queues = RabbitConstants.QueueNames.INTEGRATION_FIND_ONE)
	public Integration findIntegrationById(Long integrationId) {
		return integrationRepository.findById(integrationId).orElse(null);
	}

	@RabbitListener(queues = RabbitConstants.QueueNames.TEST_ITEMS_FIND_ONE_QUEUE)
	public TestItem findTestItem(@Payload Long itemId) {
		return testItemRepository.findById(itemId).orElse(null);
	}

	@RabbitListener(queues = RabbitConstants.QueueNames.LOGS_FIND_BY_TEST_ITEM_REF_QUEUE)
	public List<Log> findLogsByTestItem(@Header(RabbitConstants.MessageHeaders.ITEM_REF) Long itemRef,
			@Header(RabbitConstants.MessageHeaders.LIMIT) Integer limit,
			@Header(RabbitConstants.MessageHeaders.IS_LOAD_BINARY_DATA) boolean loadBinaryData) {
		return logRepository.findByTestItemId(itemRef, limit, loadBinaryData);
	}

	//	@RabbitListener(queues = RabbitConstants.QueueNames.DATA_STORAGE_FETCH_DATA_QUEUE)
	//	public BinaryData fetchData(String dataId) {
	//		return new BinaryData(dataStoreService.load(dataId));
	//	}

}
