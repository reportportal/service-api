package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.project.Project;

public interface ProjectRepository extends ReportPortalRepository<Project, Long> {

    Project findByName(String name);
}
