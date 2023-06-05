/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
class ApiKeyUtilsTest {


  @Test
  void validToken() {
    assertTrue(ApiKeyUtils.validateToken("test2_bCV0dcQfRuCo0Eq1Uv2_hizk4pHnssmV6qMLCEHGcyabsGAQxTQ1_gWJaX0kVHPM"));
  }

  @Test
  void validUUIDToken() {
    assertTrue(ApiKeyUtils.validateToken("c229070a-56fe-4f99-ad57-fa945aa9443b"));
  }

  @Test
  void invalidToken() {
    assertFalse(ApiKeyUtils.validateToken("test2_bCV0dcQfRuCo0Eq1Uv2_hizk4pHnssbsGAQxTQ1_gWJaX0kVHPM"));
  }

}