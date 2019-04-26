/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.apache.commons.collections.CollectionUtils;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_ADMINISTRATE_OBJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_READ_OBJECT;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_OWNER;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static freemarker.template.utility.Collections12.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetWidgetHandlerImpl implements GetWidgetHandler {

	private Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;

	private Map<WidgetType, LoadContentStrategy> loadContentStrategy;

	private Set<WidgetType> unfilteredWidgetTypes;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private GetUserFilterHandler getUserFilterHandler;

	@Autowired
	@Qualifier("buildFilterStrategy")
	public void setBuildFilterStrategy(Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping) {
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
	}

	@Autowired
	@Qualifier("contentLoader")
	public void setLoadContentStrategy(Map<WidgetType, LoadContentStrategy> loadContentStrategy) {
		this.loadContentStrategy = loadContentStrategy;
	}

	@Autowired
	@Qualifier("unfilteredWidgetTypes")
	public void setUnfilteredWidgetTypes(Set<WidgetType> unfilteredWidgetTypes) {
		this.unfilteredWidgetTypes = unfilteredWidgetTypes;
	}

	@Override
	@PostAuthorize(CAN_READ_OBJECT)
	public Widget getPermitted(Long widgetId, ReportPortalUser.ProjectDetails projectDetails) {
		return widgetRepository.findByIdAndProjectId(widgetId, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT,
						widgetId,
						projectDetails.getProjectName()
				));
	}

	@Override
	@PostAuthorize(CAN_ADMINISTRATE_OBJECT)
	public Widget getAdministrated(Long widgetId, ReportPortalUser.ProjectDetails projectDetails) {
		return widgetRepository.findByIdAndProjectId(widgetId, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT,
						widgetId,
						projectDetails.getProjectName()
				));
	}

	@Override
	public WidgetResource getWidget(Long widgetId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			String attributeValue) {
		Widget widget = getPermitted(widgetId, projectDetails);

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						"Unsupported widget type {}" + widget.getWidgetType()
				));

		if (widgetType != WidgetType.CUMULATIVE) {
			if (attributeValue != null) {
				throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Widget does not support two-level architecture.");
			}
		}

		Map<String, ?> content;

		if (!unfilteredWidgetTypes.contains(widgetType) && CollectionUtils.isEmpty(widget.getFilters())) {
			content = Collections.emptyMap();
		} else {
			content = buildFilterStrategyMapping.get(widgetType)
					.buildFilterAndLoadContent(loadContentStrategy.get(widgetType), projectDetails, widget, attributeValue);
		}

		WidgetResource resource = WidgetConverter.TO_WIDGET_RESOURCE.apply(widget);
		resource.setContent(content);
		return resource;
	}

	@Override
	public Map<String, ?> getWidgetPreview(WidgetPreviewRQ previewRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		WidgetType widgetType = WidgetType.findByName(previewRQ.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						"Unsupported widget type {}" + previewRQ.getWidgetType()
				));

		List<UserFilter> userFilter = null;
		if (CollectionUtils.isNotEmpty(previewRQ.getFilterIds())) {
			userFilter = getUserFilterHandler.getFiltersById(previewRQ.getFilterIds().toArray(new Long[0]), projectDetails, user);
		}

		if (!unfilteredWidgetTypes.contains(widgetType) && CollectionUtils.isEmpty(userFilter)) {
			return singletonMap(RESULT, Collections.emptyList());
		}

		Widget widget = new WidgetBuilder().addWidgetPreviewRq(previewRQ)
				.addProject(projectDetails.getProjectId())
				.addFilters(userFilter)
				.get();

		return buildFilterStrategyMapping.get(widgetType)
				.buildFilterAndLoadContent(loadContentStrategy.get(widgetType), projectDetails, widget, null);
	}

	@Override
	public Iterable<Object> getOwnNames(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user) {
		Page<Widget> own = widgetRepository.getOwn(ProjectFilter.of(filter, projectDetails.getProjectId()), pageable, user.getUsername());
		return PagedResourcesAssembler.pageConverter().apply(own.map(Widget::getName));
	}

	@Override
	public Iterable<WidgetResource> getShared(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user) {
		Page<Widget> shared = widgetRepository.getShared(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(WidgetConverter.TO_WIDGET_RESOURCE).apply(shared);
	}

	@Override
	public Iterable<WidgetResource> searchShared(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user, String term) {
		Filter termFilter = Filter.builder()
				.withTarget(Widget.class)
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_NAME))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_OWNER))
				.build();
		Page<Widget> shared = widgetRepository.getShared(ProjectFilter.of(new CompositeFilter(filter, termFilter),
				projectDetails.getProjectId()
		), pageable, user.getUsername());
		return PagedResourcesAssembler.pageConverter(WidgetConverter.TO_WIDGET_RESOURCE).apply(shared);
	}
}
