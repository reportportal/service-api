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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.WidgetCreatedEvent;
import com.epam.ta.reportportal.core.filter.UpdateUserFilterHandler;
import com.epam.ta.reportportal.core.widget.CreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.updater.WidgetPostProcessor;
import com.epam.ta.reportportal.core.widget.content.updater.validator.WidgetValidator;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.ws.converter.converters.WidgetConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateWidgetHandlerImpl implements CreateWidgetHandler {

	private final WidgetRepository widgetRepository;

	private final UserFilterRepository filterRepository;

	private final MessageBus messageBus;

	private final ShareableObjectsHandler aclHandler;

	private final UpdateUserFilterHandler updateUserFilterHandler;

	private final List<WidgetPostProcessor> widgetPostProcessors;

	private final WidgetValidator widgetContentFieldsValidator;

	@Autowired
	public CreateWidgetHandlerImpl(WidgetRepository widgetRepository, UserFilterRepository filterRepository, MessageBus messageBus,
			ShareableObjectsHandler aclHandler, UpdateUserFilterHandler updateUserFilterHandler,
			List<WidgetPostProcessor> widgetPostProcessors, WidgetValidator widgetContentFieldsValidator) {
		this.widgetRepository = widgetRepository;
		this.filterRepository = filterRepository;
		this.messageBus = messageBus;
		this.aclHandler = aclHandler;
		this.updateUserFilterHandler = updateUserFilterHandler;
		this.widgetPostProcessors = widgetPostProcessors;
		this.widgetContentFieldsValidator = widgetContentFieldsValidator;
	}

	@Override
	public EntryCreatedRS createWidget(WidgetRQ createWidgetRQ, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<UserFilter> userFilter = getUserFilters(createWidgetRQ.getFilterIds(), projectDetails.getProjectId(), user.getUsername());

		BusinessRule.expect(widgetRepository.existsByNameAndOwnerAndProjectId(createWidgetRQ.getName(),
				user.getUsername(),
				projectDetails.getProjectId()
		), BooleanUtils::isFalse).verify(ErrorType.RESOURCE_ALREADY_EXISTS, createWidgetRQ.getName());

		Widget widget = new WidgetBuilder().addWidgetRq(createWidgetRQ)
				.addProject(projectDetails.getProjectId())
				.addFilters(userFilter)
				.addOwner(user.getUsername())
				.get();

		widgetContentFieldsValidator.validate(widget);

		widgetPostProcessors.stream()
				.filter(widgetPostProcessor -> widgetPostProcessor.supports(widget))
				.forEach(widgetPostProcessor -> widgetPostProcessor.postProcess(widget));

		widgetRepository.save(widget);
		aclHandler.initAcl(widget, user.getUsername(), projectDetails.getProjectId(), BooleanUtils.isTrue(createWidgetRQ.getShare()));
		if (widget.isShared()) {
			ofNullable(widget.getFilters()).ifPresent(filters -> updateUserFilterHandler.updateSharing(filters,
					projectDetails.getProjectId(),
					widget.isShared()
			));
		}
		messageBus.publishActivity(new WidgetCreatedEvent(TO_ACTIVITY_RESOURCE.apply(widget), user.getUserId(), user.getUsername()));
		return new EntryCreatedRS(widget.getId());
	}

	private List<UserFilter> getUserFilters(List<Long> filterIds, Long projectId, String username) {
		if (CollectionUtils.isNotEmpty(filterIds)) {
			String ids = filterIds.stream().map(String::valueOf).collect(Collectors.joining(","));
			Filter defaultFilter = new Filter(UserFilter.class, Condition.IN, false, ids, CRITERIA_ID);
			List<UserFilter> userFilters = filterRepository.getPermitted(ProjectFilter.of(defaultFilter, projectId),
					Pageable.unpaged(),
					username
			).getContent();
			BusinessRule.expect(userFilters, not(List::isEmpty)).verify(ErrorType.USER_FILTER_NOT_FOUND, filterIds, projectId, username);
			return userFilters;
		}
		return Collections.emptyList();
	}
}
