/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.resource.handler.attribute.matcher;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ItemAttributeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PredicateItemAttributeTypeMatcherTest {

  private final PredicateItemAttributeTypeMatcher systemAttributeMatcher = new PredicateItemAttributeTypeMatcher(
      ItemAttribute::isSystem, ItemAttributeType.SYSTEM);
  private final PredicateItemAttributeTypeMatcher publicAttributeMatcher = new PredicateItemAttributeTypeMatcher(
      it -> !it.isSystem(), ItemAttributeType.PUBLIC);

  @Test
  void publicShouldReturnTrue() {
    final ItemAttribute publicAttribute = new ItemAttribute("k1", "v1", false);
    Assertions.assertTrue(publicAttributeMatcher.matches(publicAttribute));
  }

  @Test
  void publicShouldReturnFalse() {
    final ItemAttribute systemAttribute = new ItemAttribute("k1", "v1", true);
    Assertions.assertFalse(publicAttributeMatcher.matches(systemAttribute));
  }

  @Test
  void systemShouldReturnTrue() {
    final ItemAttribute systemAttribute = new ItemAttribute("k1", "v1", true);
    Assertions.assertTrue(systemAttributeMatcher.matches(systemAttribute));
  }

  @Test
  void systemShouldReturnFalse() {
    final ItemAttribute publicAttribute = new ItemAttribute("k1", "v1", false);
    Assertions.assertFalse(systemAttributeMatcher.matches(publicAttribute));
  }

}