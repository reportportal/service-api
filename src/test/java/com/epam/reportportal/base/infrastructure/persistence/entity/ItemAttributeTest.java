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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.google.common.collect.Sets;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ItemAttributeTest {

  @Test
  void equals() {
    final ItemAttribute one = getAttribute(1L, "key", "val", false, 1L);
    final ItemAttribute two = getAttribute(2L, "key", "val", false, 1L);
    final ItemAttribute three = getAttribute(3L, "key", "newVal", false, 1L);
    assertEquals(one, two);
    assertNotEquals(one, three);

    final ItemAttribute four = getAttribute(4L, "key", "val", false, 2L);
    assertNotEquals(one, four);
  }

  @Test
  void hashCodeTest() {
    final ItemAttribute one = getAttribute(1L, "key", "val", false, 1L);
    final ItemAttribute two = getAttribute(2L, "key", "val", false, 1L);
    final ItemAttribute three = getAttribute(3L, "key", "newVal", false, 1L);
    assertEquals(one.hashCode(), two.hashCode());
    assertNotEquals(one.hashCode(), three.hashCode());

    final ItemAttribute four = getAttribute(4L, "key", "val", false, 2L);
    assertNotEquals(one.hashCode(), four.hashCode());
  }

  @Test
  void setTest() {
    final ItemAttribute one = getAttribute(1L, "key", "val", false, 1L);
    final ItemAttribute two = getAttribute(2L, "key", "val", false, 1L);
    final ItemAttribute three = getAttribute(3L, "key", "newVal", false, 1L);
    final ItemAttribute four = getAttribute(4L, "key", "val", false, 2L);

    final HashSet<ItemAttribute> attrSet = Sets.newHashSet(one, two, three, four);
    assertEquals(3, attrSet.size());
  }

  private ItemAttribute getAttribute(Long id, String key, String value, boolean system,
      Long itemId) {
    ItemAttribute attr = new ItemAttribute();
    attr.setId(id);
    attr.setKey(key);
    attr.setValue(value);
    attr.setSystem(system);
    if (itemId != null) {
      TestItem item = new TestItem();
      item.setItemId(itemId);
      attr.setTestItem(item);
    }
    return attr;
  }
}
