package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.store.database.entity.project.Project;

public interface ProjectFindByNameHandler {

	Project findByName(String projectName);
}
