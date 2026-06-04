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

import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.reportportal.base.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;

import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetState;
import com.epam.reportportal.base.ws.converter.builders.WidgetBuilder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

/**
 * Base class for materialized view state generators, providing common view name resolution.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractViewGenerator implements ViewGenerator {

  public static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewGenerator.class);

  private static final String LAST_REFRESH = "lastRefresh";

  private final WidgetRepository widgetRepository;

  /**
   * Creates a view generator with widget state persistence.
   *
   * @param widgetRepository persistence for post-generation widget state
   */
  public AbstractViewGenerator(WidgetRepository widgetRepository) {
    this.widgetRepository = widgetRepository;
  }

  /**
   * Populates the backing SQL for the materialized view (implementation-specific).
   *
   * @param refresh        whether to drop and recreate the view
   * @param viewName       target database object name
   * @param widget         widget being materialized
   * @param launchesFilter filter on launches feeding the view
   * @param launchesSort   sort of launches in scope
   * @param params         free-form widget and generator options
   */
  protected abstract void generateView(boolean refresh, String viewName, Widget widget,
      Filter launchesFilter, Sort launchesSort,
      MultiValueMap<String, String> params);

  @Transactional
  public void generate(boolean refresh, String viewName, Widget widget, Filter launchesFilter,
      Sort launchesSort,
      MultiValueMap<String, String> params) {
    LOGGER.debug("Widget {} - {}. Generation started", widget.getWidgetType(), widget.getId());
    generateView(refresh, viewName, widget, launchesFilter, launchesSort, params);
    LOGGER.debug("Widget {} - {}. Generation finished", widget.getWidgetType(), widget.getId());
    widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.READY.getValue())
        .addOption(VIEW_NAME, viewName)
        .addOption(LAST_REFRESH, Date.from(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant()))
        .get());
    LOGGER.debug("Widget {} - {}. State updated to: {}",
        widget.getWidgetType(),
        widget.getId(),
        WidgetOptionUtil.getValueByKey(STATE, widget.getWidgetOptions())
    );
  }

}
