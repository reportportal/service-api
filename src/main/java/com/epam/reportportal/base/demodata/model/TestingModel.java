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

package com.epam.reportportal.base.demodata.model;

public class TestingModel {

  private boolean hasBefore;
  private boolean hasAfter;

  public TestingModel() {
  }

  public boolean isHasBefore() {
    return hasBefore;
  }

  public void setHasBefore(boolean hasBefore) {
    this.hasBefore = hasBefore;
  }

  public boolean isHasAfter() {
    return hasAfter;
  }

  public void setHasAfter(boolean hasAfter) {
    this.hasAfter = hasAfter;
  }
}
