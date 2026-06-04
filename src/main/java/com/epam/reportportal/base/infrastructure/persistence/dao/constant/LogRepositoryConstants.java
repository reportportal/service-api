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

package com.epam.reportportal.base.infrastructure.persistence.dao.constant;

/**
 * Alias and field names for log jOOQ queries and CTEs.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class LogRepositoryConstants {

  public static final String DISTINCT_LOGS_TABLE = "logs_table";
  public static final String ROW_NUMBER = "row_number";
  public static final String PAGE_NUMBER = "page_number";
  public static final String TIME = "time";
  public static final String LOG_LEVEL = "log_level";
  public static final String ITEM = "item";
  public static final String LOG = "log";
  public static final String TYPE = "type";
  public static final String LOGS = "logs";

  private LogRepositoryConstants() {
    //static only
  }
}
