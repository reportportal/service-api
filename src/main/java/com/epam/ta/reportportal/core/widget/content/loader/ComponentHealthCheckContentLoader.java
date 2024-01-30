/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.loader;

import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_ATTRIBUTE;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_HAS_CHILDREN;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_HAS_STATS;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_RETRY_PARENT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_STATUS;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TYPE;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.EXCLUDE_SKIPPED;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LATEST_OPTION;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import com.epam.ta.reportportal.commons.querygen.CompositeFilterCondition;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.ComponentHealthCheckContent;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ComponentHealthCheckContentLoader implements MultilevelLoadContentStrategy {

  public static final Integer MAX_LEVEL_NUMBER = 10;

  private final WidgetContentRepository widgetContentRepository;

  @Autowired
  public ComponentHealthCheckContentLoader(WidgetContentRepository widgetContentRepository) {
    this.widgetContentRepository = widgetContentRepository;
  }

  @Override
  public Map<String, Object> loadContent(List<String> contentFields,
      Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
      String[] attributes, MultiValueMap<String, String> params, int limit) {

    List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widgetOptions);
    List<String> attributeValues = ofNullable(attributes).map(Arrays::asList)
        .orElseGet(Collections::emptyList);

    int currentLevel = attributeValues.size();
    BusinessRule.expect(attributeKeys, keys -> keys.size() > currentLevel)
        .verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Incorrect level definition");

    Filter launchesFilter = GROUP_FILTERS.apply(filterSortMapping.keySet());
    Sort launchesSort = GROUP_SORTS.apply(filterSortMapping.values());

    boolean latestMode = WidgetOptionUtil.getBooleanByKey(LATEST_OPTION, widgetOptions);

    Filter testItemFilter = Filter.builder()
        .withTarget(TestItem.class)
        .withCondition(getTestItemCondition(attributeKeys, attributeValues))
        .build();

    String currentLevelKey = attributeKeys.get(currentLevel);

    List<ComponentHealthCheckContent> content = widgetContentRepository.componentHealthCheck(
        launchesFilter,
        launchesSort,
        latestMode,
        limit,
        testItemFilter,
        currentLevelKey,
        WidgetOptionUtil.getBooleanByKey(EXCLUDE_SKIPPED, widgetOptions)
    );

    return CollectionUtils.isNotEmpty(content) ? Collections.singletonMap(RESULT, content)
        : emptyMap();
  }

  private ConvertibleCondition getTestItemCondition(List<String> attributeKeys,
      List<String> attributeValues) {

    List<ConvertibleCondition> conditions = Lists.newArrayList(FilterCondition.builder()
            .eq(CRITERIA_HAS_STATS, String.valueOf(Boolean.TRUE))
            .build(),
        FilterCondition.builder().eq(CRITERIA_HAS_CHILDREN, String.valueOf(Boolean.FALSE)).build(),
        FilterCondition.builder().eq(CRITERIA_TYPE, TestItemTypeEnum.STEP.name()).build(),
        FilterCondition.builder()
            .withCondition(Condition.EXISTS)
            .withNegative(true)
            .withSearchCriteria(CRITERIA_RETRY_PARENT_ID)
            .withValue(String.valueOf(0L))
            .build(),
        FilterCondition.builder()
            .withCondition(Condition.NOT_EQUALS)
            .withNegative(false)
            .withSearchCriteria(CRITERIA_STATUS)
            .withValue(StatusEnum.IN_PROGRESS.name())
            .build()
    );

    if (CollectionUtils.isNotEmpty(attributeValues)) {
      String attributeCriteria = IntStream.range(0, attributeValues.size()).mapToObj(index -> {
        String attributeKey = attributeKeys.get(index);
        String attributeValue = attributeValues.get(index);
        return String.join(KEY_VALUE_SEPARATOR, attributeKey, attributeValue);
      }).collect(joining(","));

      conditions.add(FilterCondition.builder()
          .withCondition(Condition.HAS)
          .withNegative(false)
          .withSearchCriteria(CRITERIA_COMPOSITE_ATTRIBUTE)
          .withValue(attributeCriteria)
          .build());
    }

    return new CompositeFilterCondition(conditions);

  }
}
