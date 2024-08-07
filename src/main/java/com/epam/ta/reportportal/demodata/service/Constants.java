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

package com.epam.ta.reportportal.demodata.service;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class Constants {

  private Constants() {
    //static only
  }

  static final String NAME = "Demo Api Tests";
  static final String PACKAGE = "com.epam.ta.reportportal.demodata.";
  static final String ITEM_WITH_NESTED_STEPS_NAME = "Test with nested steps";
  static final int STORY_PROBABILITY = 30;
  static final int CONTENT_PROBABILITY = 60;
  static final int ATTRIBUTES_COUNT = 3;
}
