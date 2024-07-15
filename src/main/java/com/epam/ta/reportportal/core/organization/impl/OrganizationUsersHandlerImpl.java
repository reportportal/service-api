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

package com.epam.ta.reportportal.core.organization.impl;

import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;

import com.epam.reportportal.api.model.OrganizationUsersPage;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.OrganizationUsersHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationUsersRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class OrganizationUsersHandlerImpl implements OrganizationUsersHandler {

  private final OrganizationUsersRepositoryCustom organizationUsersRepositoryCustom;

  public OrganizationUsersHandlerImpl(
      OrganizationUsersRepositoryCustom organizationUsersRepositoryCustom) {
    this.organizationUsersRepositoryCustom = organizationUsersRepositoryCustom;
  }

  @Override
  public OrganizationUsersPage getOrganizationUsers(Queryable filter, Pageable pageable) {
    var organizationUserProfiles = organizationUsersRepositoryCustom.findByFilter(filter, pageable);

    OrganizationUsersPage organizationUsersPage =
        new OrganizationUsersPage()
            .items(organizationUserProfiles.getContent());

    return responseWithPageParameters(organizationUsersPage, pageable,
        organizationUserProfiles.getTotalElements());
  }
}
