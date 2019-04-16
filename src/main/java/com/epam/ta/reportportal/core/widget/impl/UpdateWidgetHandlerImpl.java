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

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.WidgetUpdatedEvent;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.core.widget.UpdateWidgetHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.ws.converter.converters.WidgetConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateWidgetHandlerImpl implements UpdateWidgetHandler {

	private final WidgetRepository widgetRepository;

	private final UserFilterRepository filterRepository;

	private final MessageBus messageBus;

	private final ObjectMapper objectMapper;

	private final GetWidgetHandler getWidgetHandler;

	private final ShareableObjectsHandler aclHandler;

	@Autowired
	public UpdateWidgetHandlerImpl(WidgetRepository widgetRepository, UserFilterRepository filterRepository, MessageBus messageBus,
			ObjectMapper objectMapper, GetWidgetHandler getWidgetHandler, ShareableObjectsHandler aclHandler) {
		this.widgetRepository = widgetRepository;
		this.filterRepository = filterRepository;
		this.messageBus = messageBus;
		this.objectMapper = objectMapper;
		this.getWidgetHandler = getWidgetHandler;
		this.aclHandler = aclHandler;
	}

	@Override
	public OperationCompletionRS updateWidget(Long widgetId, WidgetRQ updateRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Widget widget = getWidgetHandler.getAdministrated(widgetId, projectDetails);

		if (!widget.getName().equals(updateRQ.getName())) {
			BusinessRule.expect(widgetRepository.existsByNameAndOwnerAndProjectId(updateRQ.getName(),
					user.getUsername(),
					projectDetails.getProjectId()
			), BooleanUtils::isFalse)
					.verify(ErrorType.RESOURCE_ALREADY_EXISTS, updateRQ.getName());
		}

		WidgetActivityResource before = TO_ACTIVITY_RESOURCE.apply(widget);

		List<UserFilter> userFilter = getUserFilters(updateRQ.getFilterIds(), projectDetails.getProjectId(), user.getUsername());
		String widgetOptionsBefore = parseWidgetOptions(widget);

		widget = new WidgetBuilder(widget).addWidgetRq(updateRQ).addFilters(userFilter).get();
		widgetRepository.save(widget);

		if (before.isShared() != widget.isShared()) {
			aclHandler.updateAcl(widget, projectDetails.getProjectId(), widget.isShared());
		}

		messageBus.publishActivity(new WidgetUpdatedEvent(before,
				TO_ACTIVITY_RESOURCE.apply(widget),
				widgetOptionsBefore,
				parseWidgetOptions(widget),
				user.getUserId(),
				user.getUsername()
		));
		return new OperationCompletionRS("Widget with ID = '" + widget.getId() + "' successfully updated.");
	}

	private List<UserFilter> getUserFilters(List<Long> filterIds, Long projectId, String username) {
		if (CollectionUtils.isNotEmpty(filterIds)) {
			Filter defaultFilter = new Filter(UserFilter.class,
					Condition.IN,
					false,
					filterIds.stream().map(String::valueOf).collect(Collectors.joining(",")),
					CRITERIA_ID
			);
			return filterRepository.getPermitted(ProjectFilter.of(defaultFilter, projectId), Pageable.unpaged(), username).getContent();
		}
		return Collections.emptyList();
	}

	private String parseWidgetOptions(Widget widget) {
		try {
			return objectMapper.writeValueAsString(widget.getWidgetOptions());
		} catch (JsonProcessingException e) {
			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
					Suppliers.formattedSupplier("Error during parsing new widget options of widget with id = ", widget.getId())
			);
		}
	}
}
