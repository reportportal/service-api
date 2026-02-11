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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.model.ActivityResource;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.pattern.PatternTemplate;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.ChartStatisticsContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.CriteriaHistoryItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.CumulativeTrendChartEntry;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.FlakyCasesTableContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.LaunchesDurationContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.LaunchesTableContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.MostTimeConsumingTestCasesContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.NotPassedCasesContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.OverallStatisticsContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.PassingRateStatisticsResult;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.ProductStatusStatisticsContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.TopPatternTemplatesContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.UniqueBugContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.ComponentHealthCheckContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;

/**
 * @author Pavel Bortnik
 */
public interface WidgetContentRepository {

  /**
   * Overall statistics content loading.
   *
   * @param filter        {@link Filter}
   * @param sort          {@link Sort}
   * @param contentFields Content fields to load
   * @param latest        Load only for latest launches
   * @param limit         Limit of loaded launches
   * @return {@link OverallStatisticsContent}
   */
  OverallStatisticsContent overallStatisticsContent(Filter filter, Sort sort,
      List<String> contentFields, boolean latest, int limit);

  /**
   * Loads top limit history of items sorted in descending order by provided criteria for specified launch. Criteria
   * could be one of statistics fields. For example if criteria is 'statistics$execution$failed' and launchName is
   * 'DefaultLaunch' that is specified in the filter and limit is 20 the result will be top 20 grouped steps by uniqueId
   * of the whole launch history with 'DefaultLaunch' name sorted by count of steps with existed statistics of
   * 'statistics$execution$failed'
   *
   * @param filter         Launches filter
   * @param criteria       Criteria for example 'statistics$execution$failed'
   * @param limit          Limit of items
   * @param includeMethods Include or not test item types that have 'METHOD' or 'CLASS'
   * @return List of items, one represents history of concrete step
   */
  List<CriteriaHistoryItem> topItemsByCriteria(Filter filter, String criteria, int limit,
      boolean includeMethods);

  /**
   * Launch statistics content loading
   *
   * @param filter        {@link Filter}
   * @param contentFields Custom fields for select query building
   * @param sort          {@link Sort}
   * @param limit         Results limit
   * @return List of {@link ChartStatisticsContent}
   */
  List<ChartStatisticsContent> launchStatistics(Filter filter, List<String> contentFields,
      Sort sort, int limit);

  /**
   * Investigated statistics loading
   *
   * @param filter {@link Filter}
   * @param sort   {@link Sort}
   * @param limit  Results limit
   * @return List of{@link ChartStatisticsContent}
   */
  List<ChartStatisticsContent> investigatedStatistics(Filter filter, Sort sort, int limit);

  /**
   * Investigated statistics loading for timeline view
   *
   * @param filter {@link Filter}
   * @param sort   {@link Sort}
   * @param limit  Results limit
   * @return List of{@link ChartStatisticsContent}
   */
  List<ChartStatisticsContent> timelineInvestigatedStatistics(Filter filter, Sort sort, int limit);

  /**
   * Launch passing rate result for depending on the filter conditions
   *
   * @param launchId {@link Launch#getId()}
   * @return {@link PassingRateStatisticsResult}
   */
  PassingRateStatisticsResult passingRatePerLaunchStatistics(Long launchId);

  /**
   * Summary passing rate result for launches depending on the filter conditions
   *
   * @param filter {@link Filter}
   * @param sort   {@link Sort}
   * @param limit  Results limit
   * @return {@link PassingRateStatisticsResult}
   */
  PassingRateStatisticsResult summaryPassingRateStatistics(Filter filter, Sort sort, int limit);

  /**
   * Test cases' count trend loading
   *
   * @param filter                {@link Filter}
   * @param executionContentField Content field with table column name
   * @param sort                  {@link Sort}
   * @param limit                 Results limit
   * @return List of{@link ChartStatisticsContent}
   */
  List<ChartStatisticsContent> casesTrendStatistics(Filter filter, String executionContentField,
      Sort sort, int limit);

  /**
   * Bug trend loading
   *
   * @param filter        {@link Filter}
   * @param contentFields Custom fields for select query building
   * @param sort          {@link Sort}
   * @param limit         Results limit
   * @return List of{@link ChartStatisticsContent}
   */
  List<ChartStatisticsContent> bugTrendStatistics(Filter filter, List<String> contentFields,
      Sort sort, int limit);

  /**
   * Comparison statistics content loading for launches with specified Ids
   *
   * @param filter        {@link Filter}
   * @param contentFields Custom fields for select query building
   * @param sort          {@link Sort}
   * @param limit         Results limit
   * @return List of{@link ChartStatisticsContent}
   */
  List<ChartStatisticsContent> launchesComparisonStatistics(Filter filter,
      List<String> contentFields, Sort sort, int limit);

  /**
   * Launches duration content loading
   *
   * @param filter   {@link Filter}
   * @param sort     {@link Sort}
   * @param isLatest Flag for retrieving only latest launches
   * @param limit    Results limit
   * @return List of{@link LaunchesDurationContent}
   */
  List<LaunchesDurationContent> launchesDurationStatistics(Filter filter, Sort sort,
      boolean isLatest, int limit);

  /**
   * Not passed cases content loading
   *
   * @param filter {@link Filter}
   * @param sort   {@link Sort}
   * @param limit  Results limit
   * @return List of{@link NotPassedCasesContent}
   */
  List<NotPassedCasesContent> notPassedCasesStatistics(Filter filter, Sort sort, int limit);

  /**
   * Launches table content loading
   *
   * @param filter        {@link Filter}
   * @param contentFields Custom fields for select query building
   * @param sort          {@link Sort}
   * @param limit         Results limit
   * @return List of{@link LaunchesTableContent}
   */
  List<LaunchesTableContent> launchesTableStatistics(Filter filter, List<String> contentFields,
      Sort sort, int limit);

  /**
   * User activity content loading
   *
   * @param filter {@link Filter}
   * @param sort   {@link Sort}
   * @param limit  Results limit
   * @return List of{@link ActivityResource}
   */
  List<ActivityResource> activityStatistics(Filter filter, Sort sort, int limit);

  /**
   * Loading unique bugs content that was produced by Bug Tracking System
   *
   * @param filter   {@link Filter}
   * @param sort     {@link Sort}
   * @param isLatest Flag for retrieving only latest launches
   * @param limit    Results limit
   * @return Map grouped by ticket id as key and List of {@link UniqueBugContent} as value
   */
  Map<String, UniqueBugContent> uniqueBugStatistics(Filter filter, Sort sort, boolean isLatest,
      int limit);

  /**
   * Loading the most "flaky" test cases content
   *
   * @param filter         {@link Filter}
   * @param includeMethods Include or not test item types that have 'METHOD' or 'CLASS'
   * @param limit          Results limit
   * @return List of {@link FlakyCasesTableContent}
   */
  List<FlakyCasesTableContent> flakyCasesStatistics(Filter filter, boolean includeMethods,
      int limit);

  /**
   * Loading the product status statistics grouped by one or more {@link Filter}
   *
   * @param filterSortMapping Map of {@link Filter} as key and {@link Sort} as value to implement multiple filters logic
   *                          with own sorting
   * @param contentFields     Custom fields for select query building
   * @param customColumns     Map of the custom column name as key and {@link ItemAttribute#getKey()} as value
   * @param isLatest          Flag for retrieving only latest launches
   * @param limit             Results limit
   * @return Map grouped by filter name with {@link UserFilter#getName()} as key and list of
   * {@link ProductStatusStatisticsContent} as value
   */
  Map<String, List<ProductStatusStatisticsContent>> productStatusGroupedByFilterStatistics(
      Map<Filter, Sort> filterSortMapping,
      List<String> contentFields, Map<String, String> customColumns, boolean isLatest, int limit);

  /**
   * Loading the product status statistics grouped by {@link Launch} with combined {@link Filter}
   *
   * @param filter        {@link Filter}
   * @param contentFields Custom fields for select query building
   * @param customColumns Map of the custom column name as key and {@link ItemAttribute#getKey()} as value
   * @param sort          {@link Sort}
   * @param isLatest      Flag for retrieving only latest launches
   * @param limit         Results limit
   * @return list of {@link ProductStatusStatisticsContent}
   */
  List<ProductStatusStatisticsContent> productStatusGroupedByLaunchesStatistics(Filter filter,
      List<String> contentFields,
      Map<String, String> customColumns, Sort sort, boolean isLatest, int limit);

  /**
   * Loading the most time consuming test cases
   *
   * @param filter {@link Filter}
   * @param limit  Results limit
   * @return list of {@link MostTimeConsumingTestCasesContent}
   */
  List<MostTimeConsumingTestCasesContent> mostTimeConsumingTestCasesStatistics(Filter filter,
      int limit);

  /**
   * Load TOP-20 most matched {@link PatternTemplate} entities with matched items count, grouped by
   * {@link ItemAttribute#getValue()} and {@link PatternTemplate#getName()}
   *
   * @param filter          {@link Filter}
   * @param sort            {@link Sort}
   * @param attributeKey    {@link ItemAttribute#getKey()}
   * @param patternName     {@link PatternTemplate#getName()}
   * @param isLatest        Flag for retrieving only latest launches
   * @param launchesLimit   Launches count limit
   * @param attributesLimit Attributes count limit
   * @return The {@link List} of the {@link TopPatternTemplatesContent}
   */
  List<TopPatternTemplatesContent> patternTemplate(Filter filter, Sort sort,
      @Nullable String attributeKey, @Nullable String patternName,
      boolean isLatest, int launchesLimit, int attributesLimit);

  /**
   * Load component health check data containing items count and passing rate. Multi-level widget with
   * {@link ItemAttribute#getKey()} on each level. Previous levels are built based on
   * {@link ItemAttribute#getKey()}-{@link ItemAttribute#getValue()} pairs
   *
   * @param launchFilter    {@link Filter} with {@link FilterTarget#LAUNCH_TARGET}
   * @param launchSort      {@link Sort} for launches query
   * @param isLatest        Flag for retrieving only latest launches
   * @param launchesLimit   launches limit
   * @param testItemFilter  {@link Filter} with {@link FilterTarget#TEST_ITEM_TARGET}
   * @param currentLevelKey {@link ItemAttribute#getKey()} for query level select
   * @return {@link List} of {@link ComponentHealthCheckContent}
   */
  List<ComponentHealthCheckContent> componentHealthCheck(Filter launchFilter, Sort launchSort,
      boolean isLatest, int launchesLimit,
      Filter testItemFilter, String currentLevelKey, boolean excludeSkipped);

  /**
   * Generate a materialized view for cumulative trend chart widget.
   *
   * @param refresh       Refreshed state
   * @param viewName      View name
   * @param launchFilter  Launches filter
   * @param launchesLimit Launches limit for widget
   */
  void generateCumulativeTrendChartView(boolean refresh, String viewName, Filter launchFilter,
      Sort launchesSort, List<String> attributes,
      int launchesLimit);

  List<CumulativeTrendChartEntry> cumulativeTrendChart(String viewName, String levelAttributeKey,
      String subAttributeKey,
      String parentAttribute);

  List<Long> getCumulativeLevelRedirectLaunchIds(String viewName, String attributes);

  void generateComponentHealthCheckTable(boolean refresh, HealthCheckTableInitParams params,
      Filter launchFilter, Sort launchSort,
      int launchesLimit, boolean isLatest);

  void removeWidgetView(String viewName);

  List<HealthCheckTableContent> componentHealthCheckTable(HealthCheckTableGetParams params);
}
