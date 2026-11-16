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

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.not;
import static com.epam.reportportal.base.ws.converter.converters.WidgetConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.base.core.events.domain.WidgetCreatedEvent;
import com.epam.reportportal.base.core.filter.UpdateUserFilterHandler;
import com.epam.reportportal.base.core.widget.CreateWidgetHandler;
import com.epam.reportportal.base.core.widget.content.updater.WidgetPostProcessor;
import com.epam.reportportal.base.core.widget.content.updater.validator.WidgetValidator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserFilterRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.widget.WidgetRQ;
import com.epam.reportportal.base.ws.converter.builders.WidgetBuilder;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link com.epam.reportportal.base.core.widget.CreateWidgetHandler} that persists new
 * widgets and runs post-processing steps.
 *
 * @author Pavel Bortnik
 */
@Service
@RequiredArgsConstructor
public class CreateWidgetHandlerImpl implements CreateWidgetHandler {

  private final WidgetRepository widgetRepository;

  private final UserFilterRepository filterRepository;

  private final ApplicationEventPublisher eventPublisher;

  private final UpdateUserFilterHandler updateUserFilterHandler;

  private final List<WidgetPostProcessor> widgetPostProcessors;

  private final WidgetValidator widgetContentFieldsValidator;

  @Override
  public EntryCreatedRS createWidget(WidgetRQ createWidgetRQ,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    List<UserFilter> userFilter =
        getUserFilters(createWidgetRQ.getFilterIds(), membershipDetails.getProjectId(),
            user.getUsername()
        );

    Widget widget =
        new WidgetBuilder().addWidgetRq(createWidgetRQ).addProject(membershipDetails.getProjectId())
            .addFilters(userFilter).addOwner(user.getUsername()).get();

    widgetContentFieldsValidator.validate(widget);

    widgetPostProcessors.stream()
        .filter(widgetPostProcessor -> widgetPostProcessor.supports(widget))
        .forEach(widgetPostProcessor -> widgetPostProcessor.postProcess(widget));

    widgetRepository.save(widget);

    eventPublisher.publishEvent(
        new WidgetCreatedEvent(TO_ACTIVITY_RESOURCE.apply(widget), user.getUserId(),
            user.getUsername(), membershipDetails.getOrgId()
        ));
    return new EntryCreatedRS(widget.getId());
  }

  private List<UserFilter> getUserFilters(List<Long> filterIds, Long projectId, String username) {
    if (CollectionUtils.isNotEmpty(filterIds)) {
      var userFilters = filterRepository.findAllByIdInAndProjectId(filterIds, projectId);
      BusinessRule.expect(userFilters, not(List::isEmpty))
          .verify(ErrorType.USER_FILTER_NOT_FOUND, filterIds, projectId, username);
      return userFilters;
    }
    return Collections.emptyList();
  }
}
