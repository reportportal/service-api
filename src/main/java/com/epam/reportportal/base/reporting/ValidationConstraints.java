/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.reporting;

/**
 * Contains constants for defining validation constraints.
 *
 * @author Aliaksei_Makayed
 */
public class ValidationConstraints {

  /* 1 always exists as predefined type */
  public static final int MIN_LAUNCH_NAME_LENGTH = 1;
  public static final int MIN_TEST_ITEM_NAME_LENGTH = 1;
  public static final int MAX_TEST_ITEM_NAME_LENGTH = 1024;
  public static final int MAX_TEST_ITEM_UNIQUE_ID_LENGTH = 1024;
  public static final int MIN_ITEM_ATTRIBUTE_VALUE_LENGTH = 1;
  public static final int MIN_NAME_LENGTH = 3;
  public static final int MAX_NAME_LENGTH = 256;
  public static final int MAX_DESCRIPTION_LENGTH = 65536;
  public static final int MAX_PARAMETERS_LENGTH = 256;
  public static final int MAX_LAUNCH_DESCRIPTION_LENGTH = 2048;
  public static final int MAX_ENTITY_DESCRIPTION = 1500;
  public static final int MIN_DESCRIPTION = 1;


  private ValidationConstraints() {

  }

}
