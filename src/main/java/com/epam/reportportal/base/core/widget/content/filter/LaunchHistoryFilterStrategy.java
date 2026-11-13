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

package com.epam.reportportal.base.core.widget.content.filter;

import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;
import static java.util.stream.Collectors.joining;

import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Filter strategy that builds the launch history filter set for history-based widgets.
 *
 * @author Pavel Bortnik
 */
@Service("launchHistoryFilterStrategy")
public class LaunchHistoryFilterStrategy extends GeneralLaunchFilterStrategy {

  @Override
  protected Filter buildDefaultFilter(Widget widget, Long projectId) {
    validateWidgetOptions(widget.getWidgetOptions());
    String launchName = WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD,
        widget.getWidgetOptions());
    Filter filter = super.buildDefaultFilter(widget, projectId);
    return filter.withCondition(
            new FilterCondition(Condition.EQUALS, false, launchName, CRITERIA_NAME))
        .withCondition(new FilterCondition(
            Condition.IN,
            false,
            Stream.of(StatusEnum.FAILED, StatusEnum.PASSED, StatusEnum.STOPPED).map(Enum::name)
                .collect(joining(",")),
            CRITERIA_LAUNCH_STATUS
        ));
  }

  /**
   * Validate provided widget options. For current widget launch name should be specified.
   *
   * @param widgetOptions Map of stored widget options.
   */
  private void validateWidgetOptions(WidgetOptions widgetOptions) {
    BusinessRule.expect(WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions),
            StringUtils::isNotBlank)
        .verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
            LAUNCH_NAME_FIELD + " should be specified for widget.");
  }
}
