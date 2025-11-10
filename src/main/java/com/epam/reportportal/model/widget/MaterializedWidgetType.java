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

package com.epam.reportportal.model.widget;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public enum MaterializedWidgetType {

  COMPONENT_HEALTH_CHECK_TABLE("componentHealthCheckTable"),
  CUMULATIVE_TREND_CHART("cumulative");

  private final String type;

  MaterializedWidgetType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
}
