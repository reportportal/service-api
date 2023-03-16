/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.materialized.generator;

import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class MaterializedViewNameGenerator {

  private static final String VIEW_PREFIX = "widget";
  private static final String NAME_SEPARATOR = "_";

  public String generate(Widget widget) {
    return String.join(NAME_SEPARATOR, VIEW_PREFIX, String.valueOf(widget.getProject().getId()),
        String.valueOf(widget.getId()));
  }
}
