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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ItemAttributeConverterTest {

  @Test
  void fromResource() {
    ItemAttributeResource resource = new ItemAttributeResource("key", "val");
    final ItemAttribute itemAttribute = ItemAttributeConverter.FROM_RESOURCE.apply(resource);

    assertEquals(itemAttribute.getKey(), resource.getKey());
    assertEquals(itemAttribute.getValue(), resource.getValue());
    assertEquals(itemAttribute.isSystem(), false);
  }
}