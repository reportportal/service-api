package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.project.Project;

import java.util.Optional;

public interface ProjectRepository extends ReportPortalRepository<Project, Long> {

	Optional<Project> findByName(String name);
}
