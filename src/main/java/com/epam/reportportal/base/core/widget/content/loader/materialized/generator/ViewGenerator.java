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

package com.epam.reportportal.base.core.widget.content.loader.materialized.generator;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

/**
 * Interface for generating materialized view state entries for a widget.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ViewGenerator {

  /**
   * Builds or refreshes the materialized view backing the widget and stored options.
   *
   * @param refresh        whether to drop and recreate the view
   * @param viewName       target database view name
   * @param widget         widget owning the materialized snapshot
   * @param launchesFilter launch scope filter
   * @param launchesSort   order for launch input set
   * @param params         additional widget options
   */
  void generate(boolean refresh, String viewName, Widget widget, Filter launchesFilter,
      Sort launchesSort,
      MultiValueMap<String, String> params);
}
