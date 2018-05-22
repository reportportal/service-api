package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.ProjectRepository;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.ws.handler.ProjectFindByNameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectFindByNameHandlerImpl implements ProjectFindByNameHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Override
	public Project findByName(String projectName) {

		return projectRepository.findByName(projectName);
	}
}
