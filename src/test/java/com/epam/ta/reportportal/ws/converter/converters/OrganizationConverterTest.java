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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.entity.enums.OrganizationType;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.model.organization.OrganizationResource;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * @author Andrei Piankouski
 */
class OrganizationConverterTest {

  @Test
  void testNull() {
    assertThrows(NullPointerException.class,
        () -> OrganizationConverter.TO_ORGANIZATION_RESOURCE.apply(null));
  }

  @Test
  void testConvert() {
    final Organization org = new Organization(1L,
        Instant.now(),
        "my-org-name",
        OrganizationType.INTERNAL,
        "my-org-slug");

    final OrganizationResource organizationResource = OrganizationConverter.TO_ORGANIZATION_RESOURCE.apply(org);

    assertEquals(organizationResource.getId(), org.getId());
    assertEquals(organizationResource.getName(), org.getName());
    assertEquals(organizationResource.getSlug(), org.getSlug());
  }
}