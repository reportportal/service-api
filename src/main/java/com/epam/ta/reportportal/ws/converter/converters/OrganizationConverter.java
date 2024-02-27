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

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;

import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.model.OrganizationResource;
import java.util.function.Function;

/**
 * @author Andrei Piankouski
 */
public class OrganizationConverter {

  private OrganizationConverter() {
    //static only
  }


  public static final Function<Organization, OrganizationResource> TO_ORGANIZATION_RESOURCE =
      org -> {
        OrganizationResource orgResource = new OrganizationResource();
        orgResource.setId(org.getId());
        orgResource.setName(org.getName());
        orgResource.setSlug(org.getSlug());
        orgResource.setType(org.getOrganizationType());
        orgResource.setCreationDate(TO_DATE.apply(org.getCreationDate()));

        return orgResource;
      };
}
