/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.api.model.OrganizationProfile;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationInfo;
import com.epam.ta.reportportal.model.organization.OrganizationInfoResource;
import com.epam.ta.reportportal.model.organization.OrganizationResource;
import java.util.function.Function;

/**
 * @author Andrei Piankouski
 */
public class OrganizationConverter {

  private OrganizationConverter() {
    //static only
  }


  public static final Function<Organization, OrganizationProfile> TO_ORGANIZATION_RESOURCE =
      org -> {
        OrganizationProfile organizationProfile = new OrganizationProfile();
        organizationProfile.setId(org.getId());
        organizationProfile.setName(org.getName());
        organizationProfile.setSlug(org.getSlug());
        organizationProfile.setType(org.getOrganizationType());
        organizationProfile.setCreatedAt(org.getCreationDate());
        organizationProfile.setExternalId();
        organizationProfile.setExternalId();

        return orgResource;
      };

  public static final Function<com.epam.ta.reportportal.api.model.OrganizationInfo, com.epam.ta.reportportal.api.model.OrganizationInfo> TO_ORGANIZATION_INFO_RESOURCE = org -> {
    OrganizationInfo orgInfoResource = new OrganizationInfo();
    orgInfoResource.setId(org.getId());
    orgInfoResource.setName(org.getName());
    orgInfoResource.setSlug(org.getSlug());
    orgInfoResource.setType(org.getOrganizationType());
    orgInfoResource.setCreationDate(org.getCreationDate());
    orgInfoResource.setLastRun(org.getLastRun());
    orgInfoResource.setLaunchesQuantity(org.getLaunchesQuantity());
    orgInfoResource.setProjectsQuantity(org.getProjectsQuantity());
    orgInfoResource.setUsersQuantity(org.getUsersQuantity());

    return orgInfoResource;
  };
}
