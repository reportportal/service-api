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

package com.epam.reportportal.base.core.events.handler;

import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.REFRESH;
import static com.epam.reportportal.base.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.reportportal.base.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.events.widget.GenerateWidgetViewEvent;
import com.epam.reportportal.base.core.widget.content.BuildFilterStrategy;
import com.epam.reportportal.base.core.widget.content.loader.materialized.generator.ViewGenerator;
import com.epam.reportportal.base.core.widget.content.materialized.generator.MaterializedViewNameGenerator;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Refreshes precomputed widget content when source data changes.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GenerateWidgetViewEventHandler {

  private final WidgetRepository widgetRepository;
  private final Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;
  private final MaterializedViewNameGenerator materializedViewNameGenerator;
  private final TaskExecutor widgetViewExecutor;
  private final Map<WidgetType, ViewGenerator> viewGeneratorMapping;

  @Autowired
  public GenerateWidgetViewEventHandler(WidgetRepository widgetRepository,
      @Qualifier("buildFilterStrategy") Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping,
      MaterializedViewNameGenerator materializedViewNameGenerator,
      @Qualifier("widgetViewExecutor") TaskExecutor widgetViewExecutor,
      @Qualifier("viewGeneratorMapping") Map<WidgetType, ViewGenerator> viewGeneratorMapping) {
    this.widgetRepository = widgetRepository;
    this.buildFilterStrategyMapping = buildFilterStrategyMapping;
    this.materializedViewNameGenerator = materializedViewNameGenerator;
    this.widgetViewExecutor = widgetViewExecutor;
    this.viewGeneratorMapping = viewGeneratorMapping;
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener
  public void onApplicationEvent(GenerateWidgetViewEvent event) {
    widgetRepository.findById(event.getWidgetId()).ifPresent(widget -> {
      WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
          .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
              formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
          ));

      Map<Filter, Sort> filterSortMapping = buildFilterStrategyMapping.get(widgetType)
          .buildFilter(widget);
      Filter launchesFilter = GROUP_FILTERS.apply(filterSortMapping.keySet());
      Sort launchesSort = GROUP_SORTS.apply(filterSortMapping.values());

      ofNullable(viewGeneratorMapping.get(widgetType)).ifPresent(
          viewGenerator -> widgetViewExecutor.execute(() -> viewGenerator.generate(
              BooleanUtils.toBoolean(event.getParams().getFirst(REFRESH)),
              materializedViewNameGenerator.generate(widget),
              widget,
              launchesFilter,
              launchesSort,
              event.getParams()
          )));

    });
  }

}
