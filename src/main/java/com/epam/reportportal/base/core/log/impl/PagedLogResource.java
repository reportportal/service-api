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

package com.epam.reportportal.base.core.log.impl;

import com.epam.reportportal.base.model.log.LogResource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Log response with page boundary markers for navigation.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class PagedLogResource extends LogResource {

  private List<Map.Entry<Long, Integer>> pagesLocation;

  public PagedLogResource() {
    pagesLocation = new LinkedList<>();
  }

  public List<Map.Entry<Long, Integer>> getPagesLocation() {
    return pagesLocation;
  }

  public void setPagesLocation(List<Map.Entry<Long, Integer>> pagesLocation) {
    this.pagesLocation = pagesLocation;
  }
}
