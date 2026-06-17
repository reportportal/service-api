/*
 * Copyright 2019 EPAM Systems
 */

package com.epam.reportportal.base.core.widget.content.loader;

import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.AGGREGATE_BY_TEST_NAME;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.BREAKDOWN_TEST_NAME;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.INCLUDE_METHODS;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.LATEST_LAUNCHES_ONLY;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.LAUNCH_BREAKDOWN;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.TEST_STABILITY_FLAKINESS;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.TEST_STABILITY_PER_LAUNCH;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.reportportal.base.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.TestStabilityFlakinessAggregator.TestExecutionRow;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.TestStabilityFlakinessClassifier;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.TestStabilityFlakinessClassifier.ClassifiedTest;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.TestStabilityFlakinessClassifier.StabilityBand;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.TestStabilityFlakinessContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.TestStabilityGroupBucket;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.TestStabilityPerLaunchContent;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class TestStabilityFlakinessMultilevelContentLoader implements MultilevelLoadContentStrategy {

  private final WidgetContentRepository widgetContentRepository;

  @Autowired
  public TestStabilityFlakinessMultilevelContentLoader(
      WidgetContentRepository widgetContentRepository) {
    this.widgetContentRepository = widgetContentRepository;
  }

  @Override
  public Map<String, Object> loadContent(List<String> contentFields,
      Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions, String[] attributes,
      MultiValueMap<String, String> params, int limit) {

    List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widgetOptions);
    if (CollectionUtils.isEmpty(attributeKeys)) {
      throw new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
          "Specify at least one grouping attribute."
      );
    }

    Filter launchesFilter = GROUP_FILTERS.apply(filterSortMapping.keySet());
    Sort launchScopeSort = Sort.by(Sort.Direction.DESC, CRITERIA_START_TIME);

    boolean includeMethods = WidgetOptionUtil.getBooleanByKey(INCLUDE_METHODS,
        widgetOptions
    );

    List<String> path =
        attributes == null ? emptyList() : Arrays.stream(attributes).filter(Objects::nonNull)
            .collect(toList());

    if (path.size() > attributeKeys.size() + 1) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Too many breadcrumb segments.");
    }

    /*
     * Always load history (last N instances) for transitions and latest snapshot for row counts.
     * Widget option latestLaunchesOnly=false uses history for both (legacy full-history counts).
     */
    boolean latestLaunchesOnly = resolveLatestLaunchesOnly(widgetOptions);

    List<TestExecutionRow> historyRaw = widgetContentRepository.testStabilityRawExecutionRows(
        launchesFilter, launchScopeSort, includeMethods, limit, false);
    List<TestExecutionRow> membershipRaw = latestLaunchesOnly
        ? widgetContentRepository.testStabilityRawExecutionRows(launchesFilter,
            launchScopeSort, includeMethods, limit, true)
        : historyRaw;
    boolean useMembershipScope = latestLaunchesOnly;

    boolean aggregateByTestName =
        WidgetOptionUtil.isBooleanPresent(AGGREGATE_BY_TEST_NAME, widgetOptions)
            && WidgetOptionUtil.getBooleanByKey(AGGREGATE_BY_TEST_NAME, widgetOptions);

    Set<Long> allItemIds = Stream.concat(historyRaw.stream(), membershipRaw.stream())
        .map(TestExecutionRow::getItemId)
        .collect(Collectors.toCollection(HashSet::new));
    Map<Long, Map<String, String>> itemAttrs =
        widgetContentRepository.testStabilityFetchItemAttributes(allItemIds, attributeKeys);

    List<TestExecutionRow> historyPathRows = historyRaw.stream()
        .filter(r -> rowMatchesPath(r, attributeKeys, path, itemAttrs))
        .collect(toList());
    List<TestExecutionRow> membershipPathRows = membershipRaw.stream()
        .filter(r -> rowMatchesPath(r, attributeKeys, path, itemAttrs))
        .collect(toList());

    if (membershipPathRows.isEmpty()) {
      return ImmutableMap.of();
    }

    if (path.size() == attributeKeys.size() + 1) {
      String drillUid = path.get(attributeKeys.size());
      List<TestExecutionRow> drillHistory =
          rowsForStabilityDrill(historyPathRows, drillUid, aggregateByTestName);
      if (drillHistory.isEmpty()) {
        return ImmutableMap.of();
      }
      Map<String, ClassifiedTest> drillClassified = classifyScoped(historyPathRows,
          membershipPathRows,
          limit,
          aggregateByTestName,
          useMembershipScope
      );
      ClassifiedTest drill = drillClassified.getOrDefault(drillUid,
          drillClassified.values().stream().findFirst().orElse(null));
      if (drill == null) {
        return ImmutableMap.of();
      }
      List<TestStabilityPerLaunchContent> perLaunch =
          TestStabilityFlakinessClassifier.perLaunchBreakdown(drillHistory, limit);
      return ImmutableMap.of(RESULT, ImmutableMap.of(
          "leaf", true,
          LAUNCH_BREAKDOWN, true,
          TEST_STABILITY_PER_LAUNCH, perLaunch,
          BREAKDOWN_TEST_NAME, drill.getName()
      ));
    }

    boolean leaf = path.size() == attributeKeys.size();
    if (leaf) {
      Map<String, ClassifiedTest> classified = classifyScoped(historyPathRows,
          membershipPathRows,
          limit,
          aggregateByTestName,
          useMembershipScope
      );
      List<TestStabilityFlakinessContent> rows =
          classified.values().stream().map(ClassifiedTest::toContent).collect(toList());
      rows.sort(java.util.Comparator.comparing(TestStabilityFlakinessContent::getName,
          String.CASE_INSENSITIVE_ORDER));
      return ImmutableMap.of(RESULT,
          ImmutableMap.of("leaf", true, TEST_STABILITY_FLAKINESS, rows));
    }

    String groupKeyName = attributeKeys.get(path.size());
    Map<String, List<TestExecutionRow>> membershipByGroup = membershipPathRows.stream()
        .collect(groupingBy(r -> extractGroupForRow(r, groupKeyName, itemAttrs)));
    Map<String, List<TestExecutionRow>> historyByGroup = historyPathRows.stream()
        .collect(groupingBy(r -> extractGroupForRow(r, groupKeyName, itemAttrs)));

    List<TestStabilityGroupBucket> groups = new ArrayList<>();
    for (Map.Entry<String, List<TestExecutionRow>> e : membershipByGroup.entrySet()) {
      List<TestExecutionRow> groupHistory =
          historyByGroup.getOrDefault(e.getKey(), emptyList());
      Map<String, ClassifiedTest> groupClassified = classifyScoped(groupHistory,
          e.getValue(),
          limit,
          aggregateByTestName,
          useMembershipScope
      );
      groups.add(aggregateBucket(e.getKey(), new ArrayList<>(groupClassified.values())));
    }
    groups.sort((a, b) ->
        String.CASE_INSENSITIVE_ORDER.compare(a.getGroupLabel(), b.getGroupLabel()));

    return ImmutableMap.of(RESULT, ImmutableMap.of(
        "leaf", false,
        "groupKey", groupKeyName,
        "breadcrumbDepth", path.size(),
        "groups", groups
    ));
  }

  private static boolean resolveLatestLaunchesOnly(WidgetOptions widgetOptions) {
    // Default on: Launches page count parity; only explicit false disables two-phase scope.
    if (widgetOptions == null || widgetOptions.getOptions() == null) {
      return true;
    }
    Object value = widgetOptions.getOptions().get(LATEST_LAUNCHES_ONLY);
    if (value == null) {
      return true;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return !"false".equalsIgnoreCase(String.valueOf(value).trim());
  }

  private static Map<String, ClassifiedTest> classifyScoped(List<TestExecutionRow> historyRows,
      List<TestExecutionRow> membershipRows,
      int limit,
      boolean aggregateByTestName,
      boolean useMembershipScope) {
    if (useMembershipScope) {
      return TestStabilityFlakinessClassifier.classifyWithMembershipScope(historyRows,
          membershipRows,
          limit,
          aggregateByTestName
      );
    }
    return TestStabilityFlakinessClassifier.classify(historyRows, limit, aggregateByTestName);
  }

  private static List<TestExecutionRow> rowsForStabilityDrill(List<TestExecutionRow> raw, String key,
      boolean aggregateByTestName) {
    return raw.stream()
        .filter(r -> TestStabilityFlakinessClassifier.rowBelongsToStabilityKey(key, r,
            aggregateByTestName))
        .collect(Collectors.toList());
  }

  private static boolean rowMatchesPath(TestExecutionRow r, List<String> attributeKeys,
      List<String> path, Map<Long, Map<String, String>> itemAttrs) {
    Map<String, String> attrs = itemAttrs.getOrDefault(r.getItemId(), emptyMap());
    int depth = Math.min(path.size(), attributeKeys.size());
    for (int i = 0; i < depth; i++) {
      String ak = attributeKeys.get(i);
      String want = path.get(i);
      String act = attrs.get(ak);
      String normalized = StringUtils.isBlank(act) ? "—" : act;
      if (!want.equals(normalized)) {
        return false;
      }
    }
    return true;
  }

  private static String extractGroupForRow(TestExecutionRow r, String groupKeyName,
      Map<Long, Map<String, String>> itemAttrs) {
    Map<String, String> attrs = itemAttrs.getOrDefault(r.getItemId(), emptyMap());
    String v = attrs.get(groupKeyName);
    return StringUtils.isBlank(v) ? "—" : v;
  }

  private TestStabilityGroupBucket aggregateBucket(String label, List<ClassifiedTest> slice) {
    EnumMap<StabilityBand, Integer> bandCounts =
        new EnumMap<>(StabilityBand.class);
    for (StabilityBand b : StabilityBand.values()) {
      bandCounts.put(b, 0);
    }
    int n = slice.size();
    double flPct = 0d;
    double transSum = 0d;
    for (ClassifiedTest ct : slice) {
      bandCounts.computeIfPresent(ct.getBand(), (k, v) -> v + 1);
      flPct += ct.getFlakinessPercent();
      transSum += ct.getTransitions();
    }

    TestStabilityGroupBucket b = new TestStabilityGroupBucket();
    b.setGroupLabel(label);
    b.setStable(bandCounts.get(StabilityBand.STABLE));
    b.setFlaky(bandCounts.get(StabilityBand.FLAKY));
    b.setTransitional(bandCounts.get(StabilityBand.TRANSITIONAL));
    b.setFailed(bandCounts.get(StabilityBand.FAILED));
    b.setTotalTests(n);
    if (n > 0) {
      b.setAvgFlakinessPercent(Math.round((flPct / n) * 100d) / 100d);
      b.setAvgTransitions(Math.round((transSum / n) * 100d) / 100d);
    } else {
      b.setAvgFlakinessPercent(0d);
      b.setAvgTransitions(0d);
    }
    return b;
  }
}
