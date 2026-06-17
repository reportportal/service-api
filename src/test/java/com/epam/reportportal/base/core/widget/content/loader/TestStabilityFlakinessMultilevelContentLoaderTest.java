/*
 * Copyright 2019 EPAM Systems
 */

package com.epam.reportportal.base.core.widget.content.loader;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.AGGREGATE_BY_TEST_NAME;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.INCLUDE_METHODS;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.LATEST_LAUNCHES_ONLY;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.util.TestStabilityFlakinessAggregator.TestExecutionRow;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.TestStabilityGroupBucket;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Verifies that a test name running in multiple sub-modules is counted under each sub-module it
 * actually ran in (per-group classification), rather than only under its single global-latest
 * execution's sub-module.
 */
class TestStabilityFlakinessMultilevelContentLoaderTest {

  private final WidgetContentRepository widgetContentRepository =
      mock(WidgetContentRepository.class);

  private final TestStabilityFlakinessMultilevelContentLoader loader =
      new TestStabilityFlakinessMultilevelContentLoader(widgetContentRepository);

  @Test
  void crossSubModuleTestCountedInEachSubModule() {
    Instant base = Instant.parse("2026-01-01T00:00:00Z");

    // shared-test ran in call-retry (item 101) and, more recently, in outbound-infra (item 102)
    TestExecutionRow sharedCallRetry = row(1L, "uid-shared", "shared-test", JStatusEnum.PASSED,
        base, base, 101L, "cr_launch", 1);
    TestExecutionRow sharedOutbound = row(2L, "uid-shared", "shared-test", JStatusEnum.PASSED,
        base.plusSeconds(3600), base.plusSeconds(3600), 102L, "oi_launch", 1);
    TestExecutionRow onlyCrA = row(1L, "uid-a", "only-cr-a", JStatusEnum.PASSED, base, base, 103L,
        "cr_launch", 1);
    TestExecutionRow onlyCrB = row(1L, "uid-b", "only-cr-b", JStatusEnum.FAILED, base, base, 104L,
        "cr_launch", 1);

    List<TestExecutionRow> raw = List.of(sharedCallRetry, sharedOutbound, onlyCrA, onlyCrB);

    Map<Long, Map<String, String>> itemAttrs = new HashMap<>();
    itemAttrs.put(101L, attrs("voice", "call-retry"));
    itemAttrs.put(102L, attrs("voice", "outbound-infra"));
    itemAttrs.put(103L, attrs("voice", "call-retry"));
    itemAttrs.put(104L, attrs("voice", "call-retry"));

    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(false)))
        .thenReturn(raw);
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(true)))
        .thenReturn(raw);
    when(widgetContentRepository.testStabilityFetchItemAttributes(any(), any()))
        .thenReturn(itemAttrs);

    Map<String, Object> options = new HashMap<>();
    options.put(ATTRIBUTE_KEYS, List.of("MODULE", "SUB_MODULE"));
    options.put(AGGREGATE_BY_TEST_NAME, true);
    options.put(INCLUDE_METHODS, false);
    WidgetOptions widgetOptions = new WidgetOptions(options);

    Map<Filter, Sort> filterSortMapping =
        Map.of(new Filter(Launch.class, new ArrayList<>()), Sort.unsorted());
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    // Navigate to MODULE=voice so the next breakdown level is SUB_MODULE.
    Map<String, Object> content = loader.loadContent(List.of(), filterSortMapping, widgetOptions,
        new String[] {"voice"}, params, 10);

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) content.get(RESULT);
    assertNotNull(result, "result block present");
    assertEquals(false, result.get("leaf"));
    assertEquals("SUB_MODULE", result.get("groupKey"));

    @SuppressWarnings("unchecked")
    List<TestStabilityGroupBucket> groups =
        (List<TestStabilityGroupBucket>) result.get("groups");
    assertNotNull(groups);

    TestStabilityGroupBucket callRetry = groups.stream()
        .filter(g -> "call-retry".equals(g.getGroupLabel())).findFirst().orElseThrow();
    TestStabilityGroupBucket outboundInfra = groups.stream()
        .filter(g -> "outbound-infra".equals(g.getGroupLabel())).findFirst().orElseThrow();

    // shared-test counts in BOTH sub-modules; call-retry keeps its own two cases too.
    assertEquals(3, callRetry.getTotalTests(), "call-retry includes shared-test + 2 own cases");
    assertEquals(1, outboundInfra.getTotalTests(), "outbound-infra includes shared-test");
    assertTrue(callRetry.getTotalTests() + outboundInfra.getTotalTests() > raw.size() - 2,
        "cross sub-module test is additive across groups");
  }

  @Test
  void mergeOffSubModuleTotalsMatchModuleTotal() {
    Instant base = Instant.parse("2026-01-01T00:00:00Z");

    TestExecutionRow inA = row(1L, "uid-1", "test-one", JStatusEnum.PASSED, base, base, 201L,
        "launch_a", 1);
    TestExecutionRow inB = row(2L, "uid-2", "test-two", JStatusEnum.PASSED, base, base, 202L,
        "launch_b", 1);

    List<TestExecutionRow> raw = List.of(inA, inB);
    Map<Long, Map<String, String>> itemAttrs = new HashMap<>();
    itemAttrs.put(201L, attrs("voice", "sub-a"));
    itemAttrs.put(202L, attrs("voice", "sub-b"));

    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(false)))
        .thenReturn(raw);
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(true)))
        .thenReturn(raw);
    when(widgetContentRepository.testStabilityFetchItemAttributes(any(), any()))
        .thenReturn(itemAttrs);

    Map<String, Object> options = new HashMap<>();
    options.put(ATTRIBUTE_KEYS, List.of("MODULE", "SUB_MODULE"));
    options.put(AGGREGATE_BY_TEST_NAME, false);
    options.put(LATEST_LAUNCHES_ONLY, true);
    options.put(INCLUDE_METHODS, false);
    WidgetOptions widgetOptions = new WidgetOptions(options);

    Map<Filter, Sort> filterSortMapping =
        Map.of(new Filter(Launch.class, new ArrayList<>()), Sort.unsorted());

    Map<String, Object> moduleContent = loader.loadContent(List.of(), filterSortMapping,
        widgetOptions, new String[] {"voice"}, new LinkedMultiValueMap<>(), 10);
    @SuppressWarnings("unchecked")
    Map<String, Object> moduleResult = (Map<String, Object>) moduleContent.get(RESULT);
    @SuppressWarnings("unchecked")
    List<TestStabilityGroupBucket> subGroups =
        (List<TestStabilityGroupBucket>) moduleResult.get("groups");
    int subSum = subGroups.stream().mapToInt(TestStabilityGroupBucket::getTotalTests).sum();

    Map<String, Object> rootContent = loader.loadContent(List.of(), filterSortMapping,
        widgetOptions, new String[] {}, new LinkedMultiValueMap<>(), 10);
    @SuppressWarnings("unchecked")
    Map<String, Object> rootResult = (Map<String, Object>) rootContent.get(RESULT);
    @SuppressWarnings("unchecked")
    List<TestStabilityGroupBucket> moduleGroups =
        (List<TestStabilityGroupBucket>) rootResult.get("groups");
    TestStabilityGroupBucket voice = moduleGroups.stream()
        .filter(g -> "voice".equals(g.getGroupLabel())).findFirst().orElseThrow();

    assertEquals(voice.getTotalTests(), subSum,
        "merge off: sub-module sum should equal module total");
    assertEquals(2, voice.getTotalTests());
  }

  @Test
  void latestLaunchesOnlyFetchesHistoryAndMembershipScopes() {
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(false)))
        .thenReturn(List.of());
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(true)))
        .thenReturn(List.of());

    Map<String, Object> options = new HashMap<>();
    options.put(ATTRIBUTE_KEYS, List.of("MODULE"));
    options.put(LATEST_LAUNCHES_ONLY, true);
    options.put(INCLUDE_METHODS, false);
    WidgetOptions widgetOptions = new WidgetOptions(options);

    loader.loadContent(List.of(), Map.of(new Filter(Launch.class, new ArrayList<>()), Sort.unsorted()),
        widgetOptions, new String[] {}, new LinkedMultiValueMap<>(), 10);

    verify(widgetContentRepository).testStabilityRawExecutionRows(any(), any(), eq(false), eq(10),
        eq(false));
    verify(widgetContentRepository).testStabilityRawExecutionRows(any(), any(), eq(false), eq(10),
        eq(true));
  }

  @Test
  void launchScopeUsesWidgetItemsCountAsInstancesPerLaunchName() {
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(false)))
        .thenReturn(List.of());
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(true)))
        .thenReturn(List.of());

    Map<String, Object> options = new HashMap<>();
    options.put(ATTRIBUTE_KEYS, List.of("MODULE", "SUB_MODULE"));
    options.put(INCLUDE_METHODS, false);
    WidgetOptions widgetOptions = new WidgetOptions(options);

    Map<Filter, Sort> filterSortMapping = Map.of(
        new Filter(Launch.class, new ArrayList<>()),
        Sort.by(Sort.Direction.DESC, "number", CRITERIA_START_TIME));

    loader.loadContent(List.of(), filterSortMapping, widgetOptions, new String[] {"voice"},
        new LinkedMultiValueMap<>(), 10);

    ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
    verify(widgetContentRepository).testStabilityRawExecutionRows(any(), sortCaptor.capture(),
        eq(false), eq(10), eq(false));
    verify(widgetContentRepository).testStabilityRawExecutionRows(any(), sortCaptor.capture(),
        eq(false), eq(10), eq(true));
    Sort scopeSort = sortCaptor.getValue();
    assertEquals(1, scopeSort.toList().size());
    assertEquals(CRITERIA_START_TIME, scopeSort.toList().get(0).getProperty());
    assertEquals(Sort.Direction.DESC, scopeSort.toList().get(0).getDirection());
  }

  @Test
  void latestLaunchesOnlyCanBeDisabled() {
    when(widgetContentRepository.testStabilityRawExecutionRows(any(), any(), anyBoolean(), any(),
        eq(false)))
        .thenReturn(List.of());

    Map<String, Object> options = new HashMap<>();
    options.put(ATTRIBUTE_KEYS, List.of("MODULE"));
    options.put(LATEST_LAUNCHES_ONLY, false);
    options.put(INCLUDE_METHODS, false);
    WidgetOptions widgetOptions = new WidgetOptions(options);

    loader.loadContent(List.of(), Map.of(new Filter(Launch.class, new ArrayList<>()), Sort.unsorted()),
        widgetOptions, new String[] {}, new LinkedMultiValueMap<>(), 5);

    verify(widgetContentRepository).testStabilityRawExecutionRows(any(), any(), eq(false), eq(5),
        eq(false));
    verify(widgetContentRepository, never()).testStabilityRawExecutionRows(any(), any(), eq(false),
        eq(5), eq(true));
  }

  private static TestExecutionRow row(long launchId, String uniqueId, String name,
      JStatusEnum status, Instant launchStart, Instant itemStart, long itemId, String launchName,
      int launchNumber) {
    return new TestExecutionRow(launchId, uniqueId, name, status, launchStart, itemStart, itemId,
        launchName, launchNumber);
  }

  private static Map<String, String> attrs(String module, String subModule) {
    Map<String, String> m = new HashMap<>();
    m.put("MODULE", module);
    m.put("SUB_MODULE", subModule);
    return m;
  }
}
