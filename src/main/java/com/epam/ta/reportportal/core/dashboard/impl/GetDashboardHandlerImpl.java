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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.core.widget.WidgetConfigurationService;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.dashboard.DashboardConfigResource;
import com.epam.ta.reportportal.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.DashboardConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
@RequiredArgsConstructor
public class GetDashboardHandlerImpl implements GetDashboardHandler {

  private final DashboardRepository dashboardRepository;
  private final WidgetConfigurationService widgetConfigurationService;

  @Override
  public Page<DashboardResource> getDashboards(MembershipDetails membershipDetails,
                                               Pageable pageable, Filter filter, ReportPortalUser user) {
    var dashboards = dashboardRepository.findByFilter(
        ProjectFilter.of(filter, membershipDetails.getProjectId()), pageable);
    return PagedResourcesAssembler.pageConverter(DashboardConverter.TO_RESOURCE).apply(dashboards);
  }

  @Override
  public DashboardResource getDashboard(Long id, MembershipDetails membershipDetails) {
    var dashboard = getDashboardById(id, membershipDetails);
    return DashboardConverter.TO_RESOURCE.apply(dashboard);
  }

  @Override
  public DashboardConfigResource getDashboardConfig(Long id,
      MembershipDetails membershipDetails) {
    var dashboard = getDashboardById(id, membershipDetails);
    var widgets = widgetConfigurationService.getWidgetsConfiguration(
        dashboard, membershipDetails);
    return DashboardConfigResource.builder().widgetsConfig(widgets).build();
  }

  private Dashboard getDashboardById(Long id, MembershipDetails membershipDetails) {
    return dashboardRepository.findByIdAndProjectId(id, membershipDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT,
						id,
            membershipDetails.getProjectName()
        ));
  }
}
