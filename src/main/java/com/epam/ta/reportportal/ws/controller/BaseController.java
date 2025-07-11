/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.entity.jasper.ReportFormat.TEXT_CSV;

/**
 * This class provides basic functionalities that all controllers in the application can use.
 */
public abstract class BaseController {

  /**
   * Checks if the provided \`accept\` header value matches the export format (CSV).
   *
   * @param accept the value of the Accept header
   * @return true if the format is CSV, false otherwise
   */
  protected static boolean isExportFormat(String accept) {
    return accept.equals(TEXT_CSV.getValue());
  }
}
