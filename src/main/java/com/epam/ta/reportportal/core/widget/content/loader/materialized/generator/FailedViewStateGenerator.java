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

package com.epam.ta.reportportal.core.widget.content.loader.materialized.generator;

import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class FailedViewStateGenerator implements ViewGenerator {

  public static final Logger LOGGER = LoggerFactory.getLogger(FailedViewStateGenerator.class);

  private final ViewGenerator delegate;
  private final WidgetRepository widgetRepository;

  public FailedViewStateGenerator(ViewGenerator delegate, WidgetRepository widgetRepository) {
    this.delegate = delegate;
    this.widgetRepository = widgetRepository;
  }

  @Override
  public void generate(boolean refresh, String viewName, Widget widget, Filter launchesFilter,
      Sort launchesSort,
      MultiValueMap<String, String> params) {
    try {
      delegate.generate(refresh, viewName, widget, launchesFilter, launchesSort, params);
    } catch (Exception exc) {
      LOGGER.error("Error during view creation: " + exc.getMessage());
      widgetRepository.save(
          new WidgetBuilder(widget).addOption(STATE, WidgetState.FAILED.getValue()).get());
      LOGGER.error("Generation failed. Widget {} - {}. State updated to: {}",
          widget.getWidgetType(),
          widget.getId(),
          WidgetOptionUtil.getValueByKey(STATE, widget.getWidgetOptions())
      );
    }
  }
}
