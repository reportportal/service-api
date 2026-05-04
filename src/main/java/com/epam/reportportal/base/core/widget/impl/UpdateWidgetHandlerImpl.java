/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.widget.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.base.util.OwnedEntityUtils.validateOwnedEntityLocked;
import static com.epam.reportportal.base.ws.converter.converters.WidgetConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.base.core.events.domain.WidgetUpdatedEvent;
import com.epam.reportportal.base.core.widget.UpdateWidgetHandler;
import com.epam.reportportal.base.core.widget.content.updater.validator.WidgetValidator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ProjectFilter;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserFilterRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.activity.WidgetActivityResource;
import com.epam.reportportal.base.model.widget.ContentParameters;
import com.epam.reportportal.base.model.widget.WidgetRQ;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.ws.converter.builders.WidgetBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link com.epam.reportportal.base.core.widget.UpdateWidgetHandler} that updates widget
 * metadata and content options.
 *
 * @author Pavel Bortnik
 */
@Service
@RequiredArgsConstructor
public class UpdateWidgetHandlerImpl implements UpdateWidgetHandler {

  private final WidgetRepository widgetRepository;
  private final UserFilterRepository filterRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private final WidgetValidator widgetContentFieldsValidator;

  @Override
  public OperationCompletionRS updateWidget(Long widgetId, WidgetRQ updateRQ,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    Widget widget = widgetRepository.findByIdAndProjectId(widgetId,
            membershipDetails.getProjectId())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT, widgetId,
                membershipDetails.getProjectName()
            ));

    if (!isEligibleForLockBypass(widget, updateRQ)) {
      validateOwnedEntityLocked(widget, membershipDetails, user);
    }
    widgetContentFieldsValidator.validate(widget);

    if (!widget.getName().equals(updateRQ.getName())) {
      BusinessRule.expect(
          widgetRepository.existsByNameAndOwnerAndProjectId(updateRQ.getName(), user.getUsername(),
              membershipDetails.getProjectId()
          ), BooleanUtils::isFalse).verify(ErrorType.RESOURCE_ALREADY_EXISTS, updateRQ.getName());
    }

    WidgetActivityResource before = TO_ACTIVITY_RESOURCE.apply(widget);

    List<UserFilter> userFilter = getUserFilters(updateRQ.getFilterIds(), membershipDetails.getProjectId());
    String widgetOptionsBefore = parseWidgetOptions(widget);

    widget = new WidgetBuilder(widget)
        .addWidgetRq(updateRQ)
        .addFilters(userFilter)
        .get();
    widgetRepository.save(widget);

    eventPublisher.publishEvent(
        new WidgetUpdatedEvent(before, TO_ACTIVITY_RESOURCE.apply(widget), widgetOptionsBefore,
            parseWidgetOptions(widget), user.getUserId(), user.getUsername(),
            membershipDetails.getOrgId()
        ));
    return new OperationCompletionRS(
        "Widget with ID = '" + widget.getId() + "' successfully updated.");
  }

  private List<UserFilter> getUserFilters(List<Long> filterIds, Long projectId) {
    if (CollectionUtils.isNotEmpty(filterIds)) {
      Filter defaultFilter = new Filter(UserFilter.class, Condition.IN, false,
          filterIds.stream().map(String::valueOf).collect(Collectors.joining(",")), CRITERIA_ID
      );
      return filterRepository.findByFilter(
          ProjectFilter.of(defaultFilter, projectId), Pageable.unpaged()).getContent();
    }
    return Collections.emptyList();
  }

  private String parseWidgetOptions(Widget widget) {
    try {
      return objectMapper.writeValueAsString(widget.getWidgetOptions());
    } catch (JsonProcessingException e) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, Suppliers.formattedSupplier(
          "Error during parsing new widget options of widget with id = ", widget.getId()));
    }
  }

  private boolean isEligibleForLockBypass(Widget widget, WidgetRQ request) {
    return isMetadataUnchanged(widget, request)
        && isContentUnchanged(widget, request.getContentParameters());
  }

  private boolean isMetadataUnchanged(Widget widget, WidgetRQ request) {
    return Objects.equals(widget.getName(), request.getName())
        && Objects.equals(widget.getDescription(), request.getDescription())
        && Objects.equals(widget.getWidgetType(), request.getWidgetType());
  }

  private boolean isContentUnchanged(Widget widget, ContentParameters params) {
    if (params == null) {
      return true;
    }

    return widget.getItemsCount() == params.getItemsCount()
        && contentFieldsMatch(widget.getContentFields(), params.getContentFields())
        && widgetOptionsMatch(widget.getWidgetOptions(), params.getWidgetOptions());
  }

  private boolean contentFieldsMatch(Set<String> existing, List<String> incoming) {
    var incomingAsSet = Optional.ofNullable(incoming)
        .map(Set::copyOf)
        .orElse(null);
    return Objects.equals(existing, incomingAsSet);
  }

  private boolean widgetOptionsMatch(WidgetOptions existing, Map<String, Object> incoming) {
    var existingOptions = Optional.ofNullable(existing)
        .map(WidgetOptions::getOptions)
        .orElse(null);
    return Objects.equals(existingOptions, incoming);
  }
}
