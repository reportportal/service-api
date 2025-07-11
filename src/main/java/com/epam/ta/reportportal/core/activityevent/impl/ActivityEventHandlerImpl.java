/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.activityevent.impl;

import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;

import com.epam.reportportal.api.model.ActivitiesSearch200Response;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.activityevent.ActivityEventHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.ws.converter.converters.ActivityEventConverter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Activity Event Handler Implementation.
 *
 * @author Ryhor_Kukharenka
 */
@Service
public class ActivityEventHandlerImpl implements ActivityEventHandler {

  private static final String LENGTH_LESS_THAN_1_SYMBOL_MSG =
      "Length of the filtering string " + "'{}' is less than 1 symbol";

  private final ActivityRepository activityRepository;

  public ActivityEventHandlerImpl(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  @Override
  public ActivitiesSearch200Response getActivityEventsHistory(Queryable filter, Pageable pageable) {
    Page<Activity> activityPage = activityRepository.findByFilter(filter, pageable);

    var activities = activityPage.getContent().stream()
        .map(ActivityEventConverter.TO_ACTIVITY_RESOURCE)
        .toList();
    ActivitiesSearch200Response response = new ActivitiesSearch200Response()
        .items(activities);

    return responseWithPageParameters(response, pageable, activityPage.getTotalElements());
  }

  @Override
  public List<String> getSubjectNames(MembershipDetails membershipDetails, String value) {
    checkBusinessRuleLessThan1Symbol(value);
    return activityRepository.findSubjectNameByProjectIdAndSubjectName(
        membershipDetails.getProjectId(), value.toLowerCase());
  }

  private void checkBusinessRuleLessThan1Symbol(String value) {
    BusinessRule.expect(!value.isEmpty(), Predicates.equalTo(true)).verify(
        ErrorType.INCORRECT_FILTER_PARAMETERS,
        Suppliers.formattedSupplier(LENGTH_LESS_THAN_1_SYMBOL_MSG, value)
    );
  }
}
