package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.ws.handler.ProjectFindByNameHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.core.configs.RabbitMqConfiguration.PROJECTS_FIND_BY_NAME;

@Component
public class ProjectRepositoryConsumer {

	@Autowired
	private ProjectFindByNameHandler projectFindByNameHandler;

	@RabbitListener(queues = PROJECTS_FIND_BY_NAME)
	public Project findByName(@Payload String projectName) {

		return projectFindByNameHandler.findByName(projectName);
	}
}
