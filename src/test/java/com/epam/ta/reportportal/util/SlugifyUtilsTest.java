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

package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.util.RegexpAssertionUtil.checkRegexpPattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlugifyUtilsTest {

  @ParameterizedTest
  @CsvSource(
      value = {
          "test",
          "test test",
          "tEst teST",
          "test !@#$%^&*()_+",
          "test  test",
          "test  - -test",
          "test-test",
          "test--test",
          "test_test",
          "test__test",
          "test__test123",
      }
  )
  void slugify(String prjName) throws NoSuchMethodException {
    var slug = SlugifyUtils.slugify(prjName);

    checkRegexpPattern("getSlug", slug);
  }


}
