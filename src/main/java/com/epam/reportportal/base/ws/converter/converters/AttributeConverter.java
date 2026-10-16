/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.ws.converter.converters;

import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.reporting.AttributeResource;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import java.util.function.Function;

/**
 * Converts key/value item attributes for launches, items, and logs.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class AttributeConverter {

  public static final int MAX_ATTRIBUTE_LENGTH = 512;
  public static final Function<AttributeResource, ItemAttribute> FROM_RESOURCE = it -> {
    ItemAttribute itemAttribute = new ItemAttribute();

    String key = it.getKey();
    if (key != null && key.length() > MAX_ATTRIBUTE_LENGTH) {
      key = key.substring(0, MAX_ATTRIBUTE_LENGTH);
    }
    String value = it.getValue();
    if (value != null && value.length() > MAX_ATTRIBUTE_LENGTH) {
      value = value.substring(0, MAX_ATTRIBUTE_LENGTH);
    }
    itemAttribute.setKey(key);
    itemAttribute.setValue(value);

    if (it instanceof ItemAttributesRQ itemAttributesRQ) {
      itemAttribute.setSystem(itemAttributesRQ.isSystem());
    } else {
      itemAttribute.setSystem(false);
    }
    return itemAttribute;
  };
  public static final Function<AttributeResource, LaunchAttribute> FROM_LAUNCH_RESOURCE = it -> {
    LaunchAttribute launchAttribute = new LaunchAttribute();

    String key = it.getKey();
    if (key != null && key.length() > MAX_ATTRIBUTE_LENGTH) {
      key = key.substring(0, MAX_ATTRIBUTE_LENGTH);
    }
    String value = it.getValue();
    if (value != null && value.length() > MAX_ATTRIBUTE_LENGTH) {
      value = value.substring(0, MAX_ATTRIBUTE_LENGTH);
    }
    launchAttribute.setKey(key);
    launchAttribute.setValue(value);

    if (it instanceof ItemAttributesRQ itemAttributesRQ) {
      launchAttribute.setSystem(itemAttributesRQ.isSystem());
    } else {
      launchAttribute.setSystem(false);
    }
    return launchAttribute;
  };

  private AttributeConverter() {
    //static only
  }
}
