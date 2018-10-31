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
import com.epam.ta.reportportal.core.project.GetProjectInfoHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectInfoHandlerImpl implements GetProjectInfoHandler {

	private final ProjectRepository projectRepository;
	private final LaunchRepository launchRepository;

	@Autowired
	public GetProjectInfoHandlerImpl(ProjectRepository projectRepository, LaunchRepository launchRepository) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
	}

	@Override
	public Iterable<ProjectInfoResource> getAllProjectsInfo(Filter filter, Pageable pageable) {
		return PagedResourcesAssembler.pageConverter(ProjectConverter.TO_PROJECT_INFO_RESOURCE)
				.apply(projectRepository.findProjectInfoByFilter(filter, pageable, Mode.DEFAULT.name()));
	}

	@Override
	public ProjectInfoResource getProjectInfo(String projectId, String interval) {
		return null;
	}

	@Override
	public Map<String, List<ChartObject>> getProjectInfoWidgetContent(String projectId, String interval, String widgetId) {
		return null;
	}
}
