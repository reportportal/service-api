/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.Test;

class LaunchMappingTest {

  @Test
  void launchLogsAreRemovedByDatabaseCascadeNotByHibernate() throws Exception {
    OneToMany mapping = Launch.class.getDeclaredField("logs").getAnnotation(OneToMany.class);

    assertFalse(mapping.orphanRemoval());
    assertEquals(0, mapping.cascade().length);
  }
}
