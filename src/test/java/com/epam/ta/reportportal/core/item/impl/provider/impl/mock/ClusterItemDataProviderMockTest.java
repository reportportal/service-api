/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl.provider.impl.mock;

import static java.util.stream.Collectors.groupingBy;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.google.common.base.Suppliers;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ClusterItemDataProviderMockTest {

  private final Supplier<List<TestItem>> itemSupplier = Suppliers.memoize(this::getItems);

  public Page<TestItem> getTestItems(Queryable filter, Pageable pageable,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, Map<String, String> params) {
    final List<TestItem> testItems = itemSupplier.get();
    final List<TestItem> content = testItems.stream()
        .skip(pageable.getOffset())
        .limit(pageable.getPageSize())
        .collect(Collectors.toList());
    return new PageImpl<>(content, pageable, testItems.size());
  }

  public Set<Statistics> accumulateStatistics(Queryable filter,
      ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
      Map<String, String> params) {
    final List<TestItem> testItems = itemSupplier.get();

    return testItems.stream()
        .map(TestItem::getItemResults)
        .flatMap(r -> r.getStatistics().stream())
        .collect(groupingBy(Statistics::getStatisticsField, LinkedHashMap::new,
            Collectors.summingInt(Statistics::getCounter)))
        .entrySet()
        .stream()
        .map(entry -> new Statistics(entry.getKey(), entry.getValue()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private List<TestItem> getItems() {
    return IntStream.range(1, 21).mapToObj(this::getTestItem).collect(Collectors.toList());
  }

  private TestItem getTestItem(int index) {
    final TestItem testItem = new TestItem();
    testItem.setItemId((long) index);
    testItem.setUuid(String.valueOf(index));
    testItem.setUniqueId(String.valueOf(index));
    testItem.setTestCaseId(String.valueOf(index));
    testItem.setTestCaseHash(index);
    testItem.setName("name " + index);
    testItem.setCodeRef("ref" + index);
    testItem.setDescription("description " + index);
    testItem.setHasChildren(false);
    testItem.setType(TestItemTypeEnum.STEP);
    testItem.setHasRetries(false);
    testItem.setHasStats(true);
    testItem.setLastModified(Instant.now());
    testItem.setPath(String.valueOf(index));
    testItem.setStartTime(Instant.now());

    final Set<Parameter> parameters = getParameters(index);
    testItem.setParameters(parameters);

    final Set<ItemAttribute> attributes = getItemAttributes(index);
    testItem.setAttributes(attributes);

    final TestItemResults testItemResults = getTestItemResults((long) index);

    testItem.setItemResults(testItemResults);

    return testItem;
  }

  private Set<Parameter> getParameters(int index) {
    final Parameter parameter = new Parameter();
    parameter.setKey("param key " + index);
    parameter.setValue("param value " + index);
    return Set.of(parameter);
  }

  private Set<ItemAttribute> getItemAttributes(int index) {
    final ItemAttribute itemAttribute = new ItemAttribute();
    itemAttribute.setKey("key" + index);
    itemAttribute.setValue("value" + index);
    itemAttribute.setSystem(false);

    return Set.of(itemAttribute);
  }

  private TestItemResults getTestItemResults(Long index) {
    final TestItemResults testItemResults = new TestItemResults();
    testItemResults.setDuration(0.01);
    testItemResults.setEndTime(Instant.now());
    testItemResults.setStatus(StatusEnum.FAILED);

    final IssueEntity issueEntity = getIssueEntity(index);

    testItemResults.setIssue(issueEntity);

    final LinkedHashSet<Statistics> statistics = getStatistics();

    testItemResults.setStatistics(statistics);
    return testItemResults;
  }

  private IssueEntity getIssueEntity(Long index) {
    final IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIssueId(index);
    issueEntity.setIssueDescription("description " + index);
    issueEntity.setAutoAnalyzed(false);
    issueEntity.setIgnoreAnalyzer(false);

    final IssueType issueType = getIssueType();

    issueEntity.setIssueType(issueType);
    return issueEntity;
  }

  private IssueType getIssueType() {
    final IssueType issueType = new IssueType();
    issueType.setId(1L);
    issueType.setLocator("ti001");
    issueType.setHexColor("#ffb743");
    issueType.setLongName("To Investigate");
    issueType.setShortName("TI");

    final IssueGroup issueGroup = getIssueGroup();
    issueType.setIssueGroup(issueGroup);
    return issueType;
  }

  private IssueGroup getIssueGroup() {
    final IssueGroup issueGroup = new IssueGroup();
    issueGroup.setId(1);
    issueGroup.setTestItemIssueGroup(TestItemIssueGroup.TO_INVESTIGATE);
    return issueGroup;
  }

  private LinkedHashSet<Statistics> getStatistics() {
    return Map.of(1L,
        "statistics$executions$total",
        2L,
        "statistics$executions$passed",
        3L,
        "statistics$executions$skipped",
        4L,
        "statistics$executions$failed",
        12L,
        "statistics$defects$to_investigate$ti001"
    ).entrySet().stream().map(entry -> {
      final StatisticsField sf = new StatisticsField(entry.getValue());
      sf.setId(entry.getKey());
      return new Statistics(sf, 1);
    }).collect(Collectors.toCollection(LinkedHashSet::new));
  }

}
