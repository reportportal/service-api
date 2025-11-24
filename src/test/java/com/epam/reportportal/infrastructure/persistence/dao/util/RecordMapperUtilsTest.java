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

package com.epam.reportportal.infrastructure.persistence.dao.util;

import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JUsers.USERS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class RecordMapperUtilsTest {

  @Test
  void fieldExcludingPredicate() {

    assertFalse(
        RecordMapperUtils.fieldExcludingPredicate(USERS.LOGIN, USERS.EMAIL).test(USERS.LOGIN));
    assertTrue(
        RecordMapperUtils.fieldExcludingPredicate(USERS.LOGIN, USERS.EMAIL).test(USERS.FULL_NAME));
  }
}
