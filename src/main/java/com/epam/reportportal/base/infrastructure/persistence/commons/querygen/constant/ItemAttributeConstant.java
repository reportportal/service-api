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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant;

import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JItemAttribute;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public final class ItemAttributeConstant {

  public static final String CRITERIA_ITEM_ATTRIBUTE_KEY = "attributeKey";
  public static final String CRITERIA_ITEM_ATTRIBUTE_VALUE = "attributeValue";
  public static final String CRITERIA_ITEM_ATTRIBUTE_SYSTEM = "attributeSystem";
  public static final String CRITERIA_COMPOSITE_ATTRIBUTE = "compositeAttribute";
  public static final String CRITERIA_COMPOSITE_SYSTEM_ATTRIBUTE = "compositeSystemAttribute";
  public static final String CRITERIA_LEVEL_ATTRIBUTE = "levelAttribute";
  public static final String KEY_VALUE_SEPARATOR = ":";
  public static final JItemAttribute LAUNCH_ATTRIBUTE = JItemAttribute.ITEM_ATTRIBUTE.as(
      "launchAttribute");

  private ItemAttributeConstant() {
    //static only
  }
}
