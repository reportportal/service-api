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

package com.epam.reportportal.infrastructure.persistence.entity.filter;

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.isPresent;

import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import java.util.Arrays;
import java.util.Optional;

/**
 * Describe possible object types for {@link com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter}
 * object<br> construction and contains functionality for calculating classes objects <br> by appropriate ObjectType's
 * elements.
 *
 * @author Aliaksei_Makayed
 */
public enum ObjectType {

  Launch(Launch.class),
  TestItem(TestItem.class),
  Activity(Activity.class),
  Log(Log.class);

  private Class<?> classObject;

  ObjectType(Class<?> classObject) {
    this.classObject = classObject;
  }

  public static ObjectType getObjectTypeByName(String name) {
    Optional<ObjectType> objectType = Arrays.stream(ObjectType.values())
        .filter(type -> type.name().equalsIgnoreCase(name)).findAny();
    BusinessRule.expect(objectType, isPresent()).verify(
        ErrorType.BAD_SAVE_USER_FILTER_REQUEST,
        "Unknown Filter object type '" + name + "'. Possible types: log, launch, testItem."
    );
    return objectType.get();
  }

  public static Class<?> getTypeByName(String name) {
    Optional<ObjectType> objectType = Arrays.stream(ObjectType.values())
        .filter(type -> type.name().equalsIgnoreCase(name)).findAny();
    BusinessRule.expect(objectType, isPresent()).verify(
        ErrorType.BAD_SAVE_USER_FILTER_REQUEST,
        "Unknown Filter object type '" + name + "'. Possible types: log, launch, testItem."
    );
    return objectType.get().classObject;
  }

  public Class<?> getClassObject() {
    return classObject;
  }
}
