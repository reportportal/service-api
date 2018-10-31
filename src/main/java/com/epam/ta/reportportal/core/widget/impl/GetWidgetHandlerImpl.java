/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.IGetWidgetHandler;
import com.epam.ta.reportportal.core.widget.ShareWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetWidgetHandlerImpl implements IGetWidgetHandler {

	private Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;

	private Map<WidgetType, LoadContentStrategy> loadContentStrategy;

	@Autowired
	private ShareWidgetHandler shareWidgetHandler;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	@Qualifier("buildFilterStrategyMapping")
	public void setBuildFilterStrategy(Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping) {
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
	}

	@Autowired
	@Qualifier("contentLoader")
	public void setLoadContentStrategy(Map<WidgetType, LoadContentStrategy> loadContentStrategy) {
		this.loadContentStrategy = loadContentStrategy;
	}

	@Override
	public WidgetResource getWidget(Long widgetId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Widget widget = shareWidgetHandler.findById(widgetId);

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						"Unsupported widget type {}" + widget.getWidgetType()
				));

		Map<String, ?> content = buildFilterStrategyMapping.get(widgetType)
				.buildFilterAndLoadContent(loadContentStrategy.get(widgetType), projectDetails, widget);
		WidgetResource resource = WidgetConverter.TO_WIDGET_RESOURCE.apply(widget);
		resource.setContent(content);
		return resource;
	}

	@Override
	public Iterable<SharedEntity> getSharedWidgetNames(String userName, String projectName, Pageable pageable) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		return null;
	}

	@Override
	public Iterable<WidgetResource> getSharedWidgetsList(String userName, String projectName, Pageable pageable) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		return null;
	}

	@Override
	public List<String> getWidgetNames(String projectName, String userName) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		return null;
	}

	@Override
	public Map<String, ?> getWidgetPreview(String projectName, String userName, WidgetPreviewRQ previewRQ) {
		return null;
	}

	@Override
	public Iterable<WidgetResource> searchSharedWidgets(String term, String projectName, Pageable pageable) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		return null;
	}
}
