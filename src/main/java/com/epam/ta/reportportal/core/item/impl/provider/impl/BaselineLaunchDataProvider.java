/*
 * Copyright 2021 EPAM Systems
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
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class BaselineLaunchDataProvider implements DataProviderHandler {

  private static final String LAUNCH_ID_PARAM = "launchId";
  private static final String BASELINE_LAUNCH_ID_PARAM = "baselineLaunchId";

  private final LaunchAccessValidator launchAccessValidator;
  private final TestItemRepository testItemRepository;
  private final FilterUpdater filterUpdater;

  @Autowired
  public BaselineLaunchDataProvider(LaunchAccessValidator launchAccessValidator,
      TestItemRepository testItemRepository,
      FilterUpdater filterUpdater) {
    this.launchAccessValidator = launchAccessValidator;
    this.testItemRepository = testItemRepository;
    this.filterUpdater = filterUpdater;
  }

  @Override
  public Page<TestItem> getTestItems(Queryable filter, Pageable pageable,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, Map<String, String> params) {
    final Queryable targetFilter = getLaunchIdFilter(LAUNCH_ID_PARAM, params, projectDetails, user);
    final Queryable baselineFilter = getLaunchIdFilter(BASELINE_LAUNCH_ID_PARAM, params,
        projectDetails, user);
    filterUpdater.update(filter);

    return testItemRepository.findAllNotFromBaseline(joinFilters(targetFilter, filter),
        joinFilters(baselineFilter, filter), pageable);
  }

  @Override
  public Set<Statistics> accumulateStatistics(Queryable filter,
      ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
      Map<String, String> params) {
    final Queryable targetFilter = getLaunchIdFilter(LAUNCH_ID_PARAM, params, projectDetails, user);
    final Queryable baselineFilter = getLaunchIdFilter(BASELINE_LAUNCH_ID_PARAM, params,
        projectDetails, user);
    filterUpdater.update(filter);
    return testItemRepository.accumulateStatisticsByFilterNotFromBaseline(
        joinFilters(targetFilter, filter),
        joinFilters(baselineFilter, filter)
    );
  }

  private Queryable getLaunchIdFilter(String key, Map<String, String> params,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {
    final Long launchId = getLaunchId(key, params);
    launchAccessValidator.validate(launchId, projectDetails, user);
    return getLaunchIdFilter(launchId);
  }

  private Long getLaunchId(String key, Map<String, String> params) {
    return Optional.ofNullable(params.get(key))
        .map(ControllerUtils::safeParseLong)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Launch id must be provided for baseline items provider"
        ));
  }

  private Queryable getLaunchIdFilter(Long launchId) {
    return Filter.builder()
        .withTarget(TestItem.class)
        .withCondition(
            FilterCondition.builder().eq(CRITERIA_LAUNCH_ID, String.valueOf(launchId)).build())
        .build();
  }

  private Queryable joinFilters(Queryable... filters) {
    return new CompositeFilter(Operator.AND, filters);
  }
}
