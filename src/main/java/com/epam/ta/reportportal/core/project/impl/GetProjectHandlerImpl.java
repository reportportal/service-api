/*
 * Copyright (C) 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.project.IGetProjectHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectHandlerImpl implements IGetProjectHandler {

	private final ProjectRepository projectRepository;

	@Autowired
	public GetProjectHandlerImpl(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public Iterable<UserResource> getProjectUsers(String project, Filter filter, Pageable pageable) {
		return null;
	}

	@Override
	public ProjectResource getProject(String projectName) {
		return ProjectConverter.TO_PROJECT_RESOURCE.apply(projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName)));
	}

	@Override
	public List<String> getUserNames(String project, String value) {
		return null;
	}

	@Override
	public Page<UserResource> getUserNames(String value, Pageable pageable) {
		return null;
	}

	@Override
	public OperationCompletionRS isProjectsAvailable() {
		return null;
	}

	@Override
	public List<String> getAllProjectNames() {
		return null;
	}

	@Override
	public Map<String, Boolean> getAnalyzerIndexingStatus() {
		return null;
	}
}
