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

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ResourceAttributeHandler;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchResourceMetadataAttributeUpdater implements
    ResourceAttributeHandler<LaunchResource> {

  private final Set<String> supportedKeys;

  public LaunchResourceMetadataAttributeUpdater(Set<String> supportedKeys) {
    this.supportedKeys = supportedKeys;
  }

  @Override
  public void handle(LaunchResource resource, Collection<ItemAttribute> attributes) {
    attributes.forEach(it -> {
      if (supportedKeys.contains(it.getKey())) {
        ofNullable(resource.getMetadata()).ifPresentOrElse(metadata -> updateMetadata(it, metadata),
            () -> {
              final Map<String, Object> metadata = Maps.newHashMapWithExpectedSize(
                  supportedKeys.size());
              updateMetadata(it, metadata);
              resource.setMetadata(metadata);
            });
      }
    });
  }

  private void updateMetadata(ItemAttribute it, Map<String, Object> metadata) {
    metadata.put(it.getKey(), it.getValue());
  }
}
