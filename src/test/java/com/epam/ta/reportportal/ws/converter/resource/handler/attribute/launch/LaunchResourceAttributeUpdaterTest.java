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

package com.epam.ta.reportportal.ws.converter.resource.handler.attribute.launch;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchResourceAttributeUpdaterTest {

  private final LaunchResourceAttributeUpdater launchResourceAttributeUpdater = new LaunchResourceAttributeUpdater();

  @Test
  void shouldUpdate() {
    final LaunchResource launchResource = new LaunchResource();
    final List<ItemAttribute> attributes = List.of(new ItemAttribute("k1", "v1", false),
        new ItemAttribute("k2", "v2", false));
    launchResourceAttributeUpdater.handle(launchResource, attributes);

    final Set<ItemAttributeResource> resourceAttributes = launchResource.getAttributes();
    Assertions.assertEquals(2, resourceAttributes.size());

    final Map<String, List<ItemAttributeResource>> mapping = resourceAttributes.stream()
        .collect(groupingBy(ItemAttributeResource::getKey));

    final ItemAttributeResource firstResource = mapping.get("k1").get(0);
    final ItemAttributeResource secondResource = mapping.get("k2").get(0);

    final ItemAttribute firstAttribute = attributes.get(0);
    final ItemAttribute secondAttribute = attributes.get(1);
    shouldEqual(firstAttribute, firstResource);
    shouldEqual(secondAttribute, secondResource);

  }

  private void shouldEqual(ItemAttribute itemAttribute, ItemAttributeResource resource) {
    assertEquals(itemAttribute.getKey(), resource.getKey());
    assertEquals(itemAttribute.getValue(), resource.getValue());
  }

}