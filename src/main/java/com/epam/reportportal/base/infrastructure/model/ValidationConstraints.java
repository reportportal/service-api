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

package com.epam.reportportal.base.infrastructure.model;

/**
 * Contains constants for defining validation constraints.
 *
 * @author Aliaksei_Makayed
 */
//TODO review and move to API service
public class ValidationConstraints {

  /* 1 always exists as predefined type */
  public static final int MAX_ISSUE_TYPES_AND_SUBTYPES = 75;
  public static final int MAX_ISSUE_SUBTYPES = 15;
  public static final int MIN_COLLECTION_SIZE = 1;
  public static final int MAX_NUMBER_OF_FILTER_ENTITIES = 20;
  public static final int MIN_WIDGET_LIMIT = 1;
  public static final int MAX_WIDGET_LIMIT = 600;
  public static final int MIN_FILTER_LIMIT = 1;
  public static final int MAX_FILTER_LIMIT = 150;
  public static final int MIN_LAUNCH_NAME_LENGTH = 1;
  public static final int MAX_LAUNCH_NAME_LENGTH = 256;
  public static final int MIN_TEST_ITEM_NAME_LENGTH = 1;
  public static final int MAX_TEST_ITEM_NAME_LENGTH = 1024;
  public static final int MAX_TEST_ITEM_UNIQUE_ID_LENGTH = 1024;
  public static final int MIN_ITEM_ATTRIBUTE_VALUE_LENGTH = 1;
  public static final int MIN_NAME_LENGTH = 3;
  public static final int MAX_NAME_LENGTH = 256;
  public static final int MIN_DESCRIPTION_LENGTH = 0;
  public static final int MAX_DESCRIPTION_LENGTH = 65536;
  public static final int MAX_PARAMETERS_LENGTH = 256;
  public static final int MAX_LAUNCH_DESCRIPTION_LENGTH = 2048;
  public static final int MAX_WIDGET_NAME_LENGTH = 128;
  public static final int MAX_DASHBOARD_NAME_LENGTH = 128;
  public static final int MAX_USER_FILTER_NAME_LENGTH = 128;
  public static final int MAX_ATTRIBUTE_LENGTH = 512;
  public static final int MIN_PAGE_NUMBER = 1;
  public static final int MAX_PAGE_NUMBER = 1024;
  public static final int MAX_HISTORY_DEPTH_BOUND = 31;
  public static final int MIN_HISTORY_DEPTH_BOUND = 0;
  public static final int MAX_HISTORY_SIZE_BOUND = 31;
  public static final int MIN_LOGIN_LENGTH = 1;
  public static final int MAX_LOGIN_LENGTH = 128;
  public static final int MIN_PASSWORD_LENGTH = 8;
  public static final int MAX_PASSWORD_LENGTH = 256;
  public static final int TICKET_MIN_LOG_SIZE = 0;
  public static final int TICKET_MAX_LOG_SIZE = 50;
  public static final int MAX_CUSTOMER_LENGTH = 64;
  public static final int MIN_USER_NAME_LENGTH = 3;
  public static final int MAX_USER_NAME_LENGTH = 60;
  public static final int MAX_PHOTO_SIZE = 1024 * 1024;
  public static final int MAX_PHOTO_HEIGHT = 500;
  public static final int MAX_PHOTO_WIDTH = 300;
  public static final int MIN_DOMAIN_SIZE = 1;
  public static final int MAX_DOMAIN_SIZE = 255;
  public static final int MIN_SUBTYPE_SHORT_NAME = 1;
  public static final int MAX_SUBTYPE_SHORT_NAME = 4;
  public static final int MIN_SUBTYPE_LONG_NAME = 3;
  public static final int MAX_SUBTYPE_LONG_NAME = 55;
  public static final int MIN_ANALYSIS_PATTERN_NAME_LENGTH = 1;
  public static final int MAX_ANALYSIS_PATTERN_NAME_LENGTH = 55;
  public static final int MAX_ENTITY_DESCRIPTION = 1500;
  public static final int MIN_DESCRIPTION = 1;
  public static final int MIN_SHOULD_MATCH = 50;
  public static final int MAX_SHOULD_MATCH = 100;
  public static final int MIN_NUMBER_OF_LOG_LINES = -1;
  public static final String HEX_COLOR_REGEXP = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
  public static final String PROJECT_NAME_REGEXP = "[a-zA-Z0-9-_]+";
  public static final String USER_PASSWORD_REGEXP = "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[^a-zA-Z\\d\\s])([^\\s]){8,256}$";

  private ValidationConstraints() {

  }

}
