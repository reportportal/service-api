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

package com.epam.reportportal.core.widget.content.loader;

import static com.epam.reportportal.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;
import static com.epam.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import com.epam.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.infrastructure.persistence.entity.widget.content.PassingRateStatisticsResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PassingRatePerLaunchContentLoader implements LoadContentStrategy {

  @Autowired
  private LaunchRepository launchRepository;

  @Autowired
  private WidgetContentRepository widgetContentRepository;

  @Override
  public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping,
      WidgetOptions widgetOptions,
      int limit) {

    String launchName = WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions);
    Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

    return launchRepository.findLatestByFilter(
        filter.withCondition(new FilterCondition(Condition.EQUALS,
            false,
            launchName,
            CRITERIA_NAME
        ))).map(this::loadContent).orElseGet(Collections::emptyMap);

  }

  private Map<String, ?> loadContent(Launch launch) {
    PassingRateStatisticsResult result = widgetContentRepository.passingRatePerLaunchStatistics(
        launch.getId());
    return result.getTotal() != 0 ? singletonMap(RESULT, result) : emptyMap();
  }

}
