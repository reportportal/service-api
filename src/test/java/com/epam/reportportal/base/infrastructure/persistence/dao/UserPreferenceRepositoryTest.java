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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.UserPreferenceRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.preference.UserPreference;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/shareable/shareable-fill.sql")
class UserPreferenceRepositoryTest extends BaseMvcTest {

  @Autowired
  private UserPreferenceRepository repository;

  @Test
  void findByProjectIdAndUserId() {
    final Long adminProjectId = 1L;
    final Long adminId = 1L;

    final List<UserPreference> adminPreferences = repository.findByProjectIdAndUserId(
        adminProjectId, adminId);

    assertNotNull(adminPreferences);
    assertTrue(!adminPreferences.isEmpty());
    adminPreferences.forEach(it -> {
      assertEquals(adminId, it.getUser().getId());
      assertEquals(adminProjectId, it.getProject().getId());
    });

    final Long defaultId = 2L;
    final Long defaultProjectId = 2L;

    final List<UserPreference> defaultPreferences = repository.findByProjectIdAndUserId(
        defaultProjectId, defaultId);

    assertNotNull(defaultPreferences);
    assertTrue(!defaultPreferences.isEmpty());
    defaultPreferences.forEach(it -> {
      assertEquals(defaultId, it.getUser().getId());
      assertEquals(defaultProjectId, it.getProject().getId());
    });
  }

  @Test
  void findByProjectIdAndUserIdAndFilterId() {
    Optional<UserPreference> userPreference = repository.findByProjectIdAndUserIdAndFilterId(1L, 1L,
        1L);
    assertTrue(userPreference.isPresent());
  }

  @Test
  void findByProjectIdAndUserIdAndFilterIdNegative() {
    Optional<UserPreference> userPreference = repository.findByProjectIdAndUserIdAndFilterId(1L, 1L,
        101L);
    assertFalse(userPreference.isPresent());
  }

  @Test
  void removeByProjectIdAndUserId() {
    final Long defaultId = 2L;
    final Long defaultProjectId = 2L;

    repository.removeByProjectIdAndUserId(defaultProjectId, defaultId);

    final List<UserPreference> defaultPreferences = repository.findByProjectIdAndUserId(
        defaultProjectId, defaultId);
    assertTrue(defaultPreferences.isEmpty());
  }
}
