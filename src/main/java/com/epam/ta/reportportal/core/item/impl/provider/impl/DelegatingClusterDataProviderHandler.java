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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.item.impl.provider.DataProviderHandler;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DelegatingClusterDataProviderHandler implements DataProviderHandler {

  public static final String CLUSTER_ID_PARAM = "clusterId";

  private final Integer maxPageSize;
  private final DataProviderHandler delegate;

  public DelegatingClusterDataProviderHandler(Integer maxPageSize,
      DataProviderHandler dataProviderHandler) {
    this.maxPageSize = maxPageSize;
    this.delegate = dataProviderHandler;
  }

  @Override
  public Page<TestItem> getTestItems(Queryable filter, Pageable pageable,
      MembershipDetails membershipDetails,
      ReportPortalUser user, Map<String, String> params) {
    validateClusterCondition(filter);
    validatePageSize(pageable);
    return delegate.getTestItems(filter, pageable, membershipDetails, user, params);
  }

  @Override
  public Set<Statistics> accumulateStatistics(Queryable filter,
      MembershipDetails membershipDetails, ReportPortalUser user,
      Map<String, String> params) {
    validateClusterCondition(filter);
    return delegate.accumulateStatistics(filter, membershipDetails, user, params);
  }

  private void validateClusterCondition(Queryable filter) {
    final boolean hasClusterIdCondition = filter.getFilterConditions()
        .stream()
        .flatMap(c -> c.getAllConditions().stream())
        .anyMatch(c -> c.getSearchCriteria().contains(CLUSTER_ID_PARAM));
    BusinessRule.expect(hasClusterIdCondition, BooleanUtils::isTrue)
        .verify(ErrorType.BAD_REQUEST_ERROR, "Cluster id condition not provided");
  }

  private void validatePageSize(Pageable pageable) {
    BusinessRule.expect(pageable.getPageSize(), pageSize -> pageSize <= maxPageSize)
        .verify(ErrorType.BAD_REQUEST_ERROR, "Max page size: " + maxPageSize);
  }
}
