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

package com.epam.reportportal.core.widget.content.updater.validator;

import static com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.reportportal.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.infrastructure.persistence.entity.widget.WidgetType;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("widgetContentFieldsValidator")
public class WidgetContentFieldsValidator implements WidgetValidator {

  private Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;

  private Map<WidgetType, WidgetValidatorStrategy> widgetValidatorLoader;

  private Map<WidgetType, MultilevelValidatorStrategy> multilevelValidatorLoader;

  @Autowired
  @Qualifier("buildFilterStrategy")
  public void setBuildFilterStrategy(
      Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping) {
    this.buildFilterStrategyMapping = buildFilterStrategyMapping;
  }

  @Autowired
  @Qualifier("widgetValidatorLoader")
  public void setWidgetValidatorLoader(
      Map<WidgetType, WidgetValidatorStrategy> widgetValidatorLoader) {
    this.widgetValidatorLoader = widgetValidatorLoader;
  }

  @Autowired
  @Qualifier("multilevelValidatorLoader")
  public void setMultilevelValidatorLoader(
      Map<WidgetType, MultilevelValidatorStrategy> multilevelValidatorLoader) {
    this.multilevelValidatorLoader = multilevelValidatorLoader;
  }

  @Override
  public void validate(Widget widget) {
    WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
            formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
        ));

    if (widgetType.isSupportMultilevelStructure()) {
      multilevelValidatorLoader.get(widgetType)
          .validate(Lists.newArrayList(widget.getContentFields()),
              buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
              widget.getWidgetOptions(),
              null,
              null,
              widget.getItemsCount()
          );
    } else {
      widgetValidatorLoader.get(widgetType)
          .validate(Lists.newArrayList(
                  ofNullable(widget.getContentFields()).orElse(Collections.emptySet())),
              buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
              widget.getWidgetOptions(),
              widget.getItemsCount()
          );
    }
  }
}
