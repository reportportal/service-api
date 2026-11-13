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

import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.LATEST_OPTION;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Generates the materialized view state entry for the Component Health Check Table widget.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class HealthCheckTableGenerator extends AbstractViewGenerator {

  public static final String CUSTOM_COLUMN = "customColumn";

  private final WidgetContentRepository widgetContentRepository;

  @Autowired
  public HealthCheckTableGenerator(WidgetRepository widgetRepository,
      WidgetContentRepository widgetContentRepository) {
    super(widgetRepository);
    this.widgetContentRepository = widgetContentRepository;
  }

  @Override
  protected void generateView(boolean refresh, String viewName, Widget widget,
      Filter launchesFilter, Sort launchesSort,
      MultiValueMap<String, String> params) {
    widgetContentRepository.generateComponentHealthCheckTable(refresh,
        getInitParams(widget, viewName),
        launchesFilter,
        launchesSort,
        widget.getItemsCount(),
        WidgetOptionUtil.getBooleanByKey(LATEST_OPTION, widget.getWidgetOptions())
    );
  }

  private HealthCheckTableInitParams getInitParams(Widget widget, String viewName) {
    List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS,
        widget.getWidgetOptions());
    return ofNullable(WidgetOptionUtil.getValueByKey(CUSTOM_COLUMN, widget.getWidgetOptions())).map(
            custom -> HealthCheckTableInitParams
                .of(viewName, attributeKeys, custom))
        .orElseGet(() -> HealthCheckTableInitParams.of(viewName, attributeKeys));
  }
}
