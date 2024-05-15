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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Handler for attributes
 *
 * @author Ivan Kustau
 */
@Component
public class AttributeHandler {

  /**
   * Handle attributes for launch.
   *
   * @param launch Launch which attributes should be handled
   */
  public void handleAttributes(Launch launch) {
    if (launch == null || launch.getAttributes() == null) {
      return;
    }

    handleRetentionPolicyAttribute(launch);
  }

  private void handleRetentionPolicyAttribute(Launch launch) {
    Set<ItemAttribute> attributes = launch.getAttributes();
    ItemAttribute importantAttribute = null;
    ItemAttribute regularAttribute = null;

    for (ItemAttribute attribute : attributes) {
      if (attribute.isSystem() && "retentionPolicy".equals(attribute.getKey())) {
        if ("important".equals(attribute.getValue())) {
          importantAttribute = attribute;
        } else if ("regular".equals(attribute.getValue())) {
          regularAttribute = attribute;
        }
      }
    }

    if (importantAttribute != null && regularAttribute != null) {
      // If both 'important' and 'regular' attributes are present, remove 'regular'
      attributes.remove(regularAttribute);
      launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
    } else if (importantAttribute != null) {
      // If only 'important' attribute is present
      launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
    } else if (regularAttribute != null) {
      // If only 'regular' attribute is present
      launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
    }
  }
}
