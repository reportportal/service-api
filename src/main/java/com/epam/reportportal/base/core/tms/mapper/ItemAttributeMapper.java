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

package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper utility for converting test case attributes to ItemAttribute entities.
 *
 * @author ReportPortal
 */
@Slf4j
@Component
public class ItemAttributeMapper {

  /**
   * Converts TmsTestCaseAttributeRS set to ItemAttribute set.
   * Attributes are marked as non-system (system=false) since they come from test cases.
   *
   * @param tmsAttributes TMS test case attributes
   * @param testItem test item to attach attributes to
   * @param launch launch entity for context
   * @return set of ItemAttribute entities
   */
  public Set<ItemAttribute> mapTestCaseAttributesToItemAttributes(
      Set<TmsTestCaseAttributeRS> tmsAttributes,
      TestItem testItem,
      Launch launch) {

    var itemAttributes = new HashSet<ItemAttribute>();

    if (tmsAttributes == null || tmsAttributes.isEmpty()) {
      log.debug("No test case attributes to map");
      return itemAttributes;
    }

    log.debug("Mapping {} test case attributes to item attributes", tmsAttributes.size());

    for (var tmsAttr : tmsAttributes) {
      if (tmsAttr.getKey() != null && !tmsAttr.getKey().isEmpty()) {
        var itemAttr = new ItemAttribute(tmsAttr.getKey(), tmsAttr.getValue(), false);
        itemAttr.setTestItem(testItem);
        itemAttr.setLaunch(launch);
        itemAttributes.add(itemAttr);
        log.trace("Mapped attribute: {} = {}", tmsAttr.getKey(), tmsAttr.getValue());
      } else {
        log.warn("Skipping test case attribute with null or empty key");
      }
    }

    log.debug("Successfully mapped {} attributes", itemAttributes.size());
    return itemAttributes;
  }

  /**
   * Validates that attribute key-value pair is valid.
   *
   * @param key attribute key
   * @param value attribute value
   * @return true if valid, false otherwise
   */
  public boolean isValidAttribute(String key, String value) {
    return key != null && !key.isEmpty();
  }
}
