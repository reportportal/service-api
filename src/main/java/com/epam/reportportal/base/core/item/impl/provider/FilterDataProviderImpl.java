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

package com.epam.reportportal.base.core.item.impl.provider;

import static com.epam.reportportal.base.ws.controller.TestItemController.IS_LATEST_LAUNCHES_REQUEST_PARAM;
import static com.epam.reportportal.base.ws.controller.TestItemController.LAUNCHES_LIMIT_REQUEST_PARAM;

import com.epam.reportportal.base.core.item.utils.DefaultLaunchFilterProvider;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserFilterRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.ControllerUtils;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Test item listing driven by a saved or ad-hoc filter.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class FilterDataProviderImpl implements DataProviderHandler {

  private static final String FILTER_ID_PARAM = "filterId";

  @Autowired
  private UserFilterRepository filterRepository;

  @Autowired
  private TestItemRepository testItemRepository;

  @Override
  public Set<Statistics> accumulateStatistics(Queryable filter, MembershipDetails membershipDetails,
      ReportPortalUser user,
      Map<String, String> params) {
    Optional.ofNullable(params.get(FILTER_ID_PARAM))
        .map(ControllerUtils::safeParseLong)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Filter id must be provided for filter based items provider"
        ));
    return testItemRepository.accumulateStatisticsByFilter(filter);
  }

  @Override
  public Page<TestItem> getTestItems(Queryable filter, Pageable pageable, MembershipDetails membershipDetails,
      ReportPortalUser user, Map<String, String> params) {
    Long launchFilterId = Optional.ofNullable(params.get(FILTER_ID_PARAM))
        .map(ControllerUtils::safeParseLong)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Filter id must be provided for filter based items provider"
        ));

    Integer launchesLimit = Optional.ofNullable(params.get(LAUNCHES_LIMIT_REQUEST_PARAM))
        .map(ControllerUtils::safeParseInt)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Launches limit must be provided for filter based items provider"
        ));

    Boolean isLatest = Optional.ofNullable(params.get(IS_LATEST_LAUNCHES_REQUEST_PARAM)).map(Boolean::parseBoolean)
        .orElse(false);

    UserFilter userFilter = filterRepository.findByIdAndProjectId(launchFilterId, membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT,
            launchFilterId,
            membershipDetails.getProjectName()
        ));

    Pair<Queryable, Pageable> queryablePair = DefaultLaunchFilterProvider.createDefaultLaunchQueryablePair(
        membershipDetails,
        userFilter,
        launchesLimit
    );

    return testItemRepository.findByFilter(isLatest, queryablePair.getKey(), filter, queryablePair.getValue(),
        pageable);
  }

}
