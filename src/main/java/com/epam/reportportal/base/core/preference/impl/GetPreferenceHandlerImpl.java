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

package com.epam.reportportal.base.core.preference.impl;

import com.epam.reportportal.base.core.preference.GetPreferenceHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserPreferenceRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.preference.UserPreference;
import com.epam.reportportal.base.model.filter.UserFilterResource;
import com.epam.reportportal.base.model.preference.PreferenceResource;
import com.epam.reportportal.base.ws.converter.converters.UserFilterConverter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link com.epam.reportportal.base.core.preference.GetPreferenceHandler}
 *
 * @author Dzmitry_Kavalets
 */
@Service
public class GetPreferenceHandlerImpl implements GetPreferenceHandler {

  private final UserPreferenceRepository userPreferenceRepository;

  @Autowired
  public GetPreferenceHandlerImpl(UserPreferenceRepository userPreferenceRepository) {
    this.userPreferenceRepository = userPreferenceRepository;
  }

  @Override
  public PreferenceResource getPreference(MembershipDetails membershipDetails,
      ReportPortalUser user) {
    List<UserPreference> userPreferences =
        userPreferenceRepository.findByProjectIdAndUserId(membershipDetails.getProjectId(),
            user.getUserId()
        );
    PreferenceResource preferenceResource = new PreferenceResource();
    preferenceResource.setUserId(user.getUserId());
    preferenceResource.setProjectId(membershipDetails.getProjectId());
    List<UserFilterResource> filters = userPreferences.stream()
        .map(it -> UserFilterConverter.TO_FILTER_RESOURCE.apply(it.getFilter()))
        .collect(Collectors.toList());
    preferenceResource.setFilters(filters);
    return preferenceResource;
  }
}
