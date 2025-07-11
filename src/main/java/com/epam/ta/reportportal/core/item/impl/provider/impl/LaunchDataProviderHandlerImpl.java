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

package com.epam.ta.reportportal.core.item.impl.provider.impl;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.impl.filter.updater.FilterUpdater;
import com.epam.ta.reportportal.core.item.impl.provider.DataProviderHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchDataProviderHandlerImpl implements DataProviderHandler {

  private static final String LAUNCH_ID_PARAM = "launchId";

  @Autowired
  private LaunchAccessValidator launchAccessValidator;

  @Autowired
  private TestItemRepository testItemRepository;

  @Autowired
  private FilterUpdater filterUpdater;

  @Override
  public Page<TestItem> getTestItems(Queryable filter, Pageable pageable,
      MembershipDetails membershipDetails,
      ReportPortalUser user, Map<String, String> params) {
    filter = updateFilter(filter, membershipDetails, user, params);
    return testItemRepository.findByFilter(filter, pageable);
  }

  @Override
  public Set<Statistics> accumulateStatistics(Queryable filter,
      MembershipDetails membershipDetails, ReportPortalUser user,
      Map<String, String> params) {
    filter = updateFilter(filter, membershipDetails, user, params);
    return testItemRepository.accumulateStatisticsByFilter(filter);
  }

  private Queryable updateFilter(Queryable filter, MembershipDetails membershipDetails,
      ReportPortalUser user,
      Map<String, String> params) {
    Long launchId = Optional.ofNullable(params.get(LAUNCH_ID_PARAM))
        .map(ControllerUtils::safeParseLong)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Launch id must be provided for launch based items provider"
        ));
    launchAccessValidator.validate(launchId, membershipDetails, user);
    Queryable launchBasedFilter = Filter.builder()
        .withTarget(TestItem.class)
        .withCondition(
            FilterCondition.builder().eq(CRITERIA_LAUNCH_ID, String.valueOf(launchId)).build())
        .build();

    filterUpdater.update(filter);

    return new CompositeFilter(Operator.AND, filter, launchBasedFilter);
  }
}
