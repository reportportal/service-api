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

package com.epam.reportportal.base.infrastructure.persistence.dao.organization;

import com.epam.reportportal.base.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationProfile;
import java.util.Optional;

/**
 * Repository interface for searching and filtering organization records.
 *
 * @author Siarhei Hrabko
 */
public interface OrganizationRepositoryCustom extends FilterableRepository<OrganizationProfile> {

  Optional<Organization> findById(Long orgId);

  Optional<Organization> findOrganizationByName(String name);

  Optional<Organization> findOrganizationBySlug(String slug);

}
