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

import static java.util.stream.Collectors.toSet;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ResourceAttributeHandler;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import java.util.Collection;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchResourceAttributeUpdater implements ResourceAttributeHandler<LaunchResource> {

  @Override
  public void handle(LaunchResource resource, Collection<ItemAttribute> attributes) {
    resource.setAttributes(
        attributes.stream().map(it -> new ItemAttributeResource(it.getKey(), it.getValue()))
            .collect(toSet()));
  }
}
