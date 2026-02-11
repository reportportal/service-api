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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.ws.BaseMvcTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Siarhei Hrabko
 */
class OrganizationUserRepositoryTest extends BaseMvcTest {

  @Autowired
  private OrganizationUserRepository organizationUserRepository;

  @ParameterizedTest
  @CsvSource(value = {
      "1|1|Member",
      "2|1|Member",
  }, delimiter = '|')
  void organizationUser(Long userId, Long orgId, String role) {
    var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId).get();
    assertNotNull(orgUser);
    assertEquals(role, orgUser.getOrganizationRole().getRoleName());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "1|456",
      "456|1",
      "456|654",
  }, delimiter = '|')
  void organizationUserNotFound(Long userId, Long orgId) {
    var orgUserOptional = organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId);
    assertTrue(orgUserOptional.isEmpty());
  }

}
