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

package com.epam.ta.reportportal.ws.converter.builders;

import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.FROM_RESOURCE;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.reporting.ParameterResource;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class TestItemBuilder implements Supplier<TestItem> {

  private static final int TEST_ITEM_DESCRIPTION_LENGTH_LIMIT = 2048;
  private static final int DESCRIPTION_START_SYMBOL_INDEX = 0;
  public static final String PARAMETER_NULL_VALUE = "NULL";

  private TestItem testItem;

  public TestItemBuilder() {
    testItem = new TestItem();
  }

  public TestItemBuilder(TestItem testItem) {
    this.testItem = testItem;
  }

  public TestItemBuilder addStartItemRequest(StartTestItemRQ rq) {

    testItem.setStartTime(rq.getStartTime());
    testItem.setName(rq.getName().trim());
    testItem.setUniqueId(rq.getUniqueId());
    testItem.setUuid(Optional.ofNullable(rq.getUuid()).orElse(UUID.randomUUID().toString()));
    testItem.setHasStats(rq.isHasStats());

    TestCaseIdEntry testCaseIdEntry = processTestCaseId(rq);
    testItem.setTestCaseId(testCaseIdEntry.getId());
    testItem.setTestCaseHash(testCaseIdEntry.getHash());

    testItem.setCodeRef(rq.getCodeRef());

    TestItemResults testItemResults = new TestItemResults();
    testItemResults.setStatus(StatusEnum.IN_PROGRESS);

    testItemResults.setTestItem(testItem);
    testItem.setItemResults(testItemResults);

    addDescription(rq.getDescription());
    addParameters(rq.getParameters());
    addType(rq.getType());
    return this;
  }

  public TestItemBuilder addLaunchId(Long launchId) {
    testItem.setLaunchId(launchId);
    return this;
  }

  public TestItemBuilder addParentId(Long parentId) {
    testItem.setParentId(parentId);
    return this;
  }

  public TestItemBuilder addType(String typeValue) {
    TestItemTypeEnum type = TestItemTypeEnum.fromValue(typeValue)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.UNSUPPORTED_TEST_ITEM_TYPE, typeValue));
    testItem.setType(type);
    return this;
  }

  public TestItemBuilder addDescription(String description) {
    ofNullable(description).ifPresent(it -> testItem.setDescription(StringUtils.substring(it.trim(),
        DESCRIPTION_START_SYMBOL_INDEX,
        TEST_ITEM_DESCRIPTION_LENGTH_LIMIT
    )));
    return this;
  }

  public TestItemBuilder addStatus(StatusEnum statusEnum) {
    testItem.getItemResults().setStatus(statusEnum);
    return this;
  }

  public TestItemBuilder addTestCaseId(@Nullable String testCaseId) {
    ofNullable(testCaseId).map(caseId -> new TestCaseIdEntry(testCaseId, testCaseId.hashCode()))
        .ifPresent(entry -> {
          testItem.setTestCaseId(entry.getId());
          testItem.setTestCaseHash(entry.getHash());
        });
    return this;
  }

  public TestItemBuilder addAttributes(Set<ItemAttributesRQ> attributes) {
    ofNullable(attributes).ifPresent(it -> testItem.getAttributes().addAll(it.stream().map(val -> {
      ItemAttribute itemAttribute = FROM_RESOURCE.apply(val);
      itemAttribute.setTestItem(testItem);
      return itemAttribute;
    }).collect(Collectors.toSet())));
    return this;
  }

  public TestItemBuilder overwriteAttributes(Set<? extends ItemAttributeResource> attributes) {
    if (attributes != null) {
      final Set<ItemAttribute> overwrittenAttributes = testItem.getAttributes()
          .stream()
          .filter(ItemAttribute::isSystem)
          .collect(Collectors.toSet());
      attributes.stream().map(val -> {
        ItemAttribute itemAttribute = FROM_RESOURCE.apply(val);
        itemAttribute.setTestItem(testItem);
        return itemAttribute;
      }).forEach(overwrittenAttributes::add);
      testItem.setAttributes(overwrittenAttributes);
    }
    return this;
  }

  public TestItemBuilder addTestItemResults(TestItemResults testItemResults) {
    checkNotNull(testItemResults, "Provided value shouldn't be null");
    testItem.setItemResults(testItemResults);
    addDuration(testItemResults.getEndTime());
    return this;
  }

  public TestItemBuilder addDuration(Instant endTime) {
    checkNotNull(endTime, "Provided value shouldn't be null");
    checkNotNull(testItem.getItemResults(), "Test item results shouldn't be null");

    //converts to seconds
    testItem.getItemResults()
        .setDuration(ChronoUnit.MILLIS.between(testItem.getStartTime(), endTime) / 1000d);
    return this;
  }

  public TestItemBuilder addParameters(List<ParameterResource> parameters) {
    if (!CollectionUtils.isEmpty(parameters)) {
      testItem.setParameters(parameters.stream().map(it -> {
        Parameter parameter = new Parameter();
        parameter.setKey(it.getKey());
        parameter.setValue(ofNullable(it.getValue()).orElse(PARAMETER_NULL_VALUE));
        return parameter;
      }).collect(Collectors.toSet()));
    }
    return this;
  }

  public static TestCaseIdEntry processTestCaseId(StartTestItemRQ startTestItemRQ) {
    final String testCaseId = startTestItemRQ.getTestCaseId();
    if (Objects.nonNull(testCaseId)) {
      return new TestCaseIdEntry(testCaseId, testCaseId.hashCode());
    } else {
      final String codeRef = startTestItemRQ.getCodeRef();
      if (Objects.nonNull(codeRef)) {
        String id = compose(codeRef, startTestItemRQ.getParameters());
        return new TestCaseIdEntry(id, id.hashCode());
      }
    }
    return TestCaseIdEntry.empty();
  }

  private static String compose(String codeRef, List<ParameterResource> parameters) {
    return CollectionUtils.isEmpty(parameters) ?
        codeRef :
        codeRef + "[" + parameters.stream().map(ParameterResource::getValue)
            .collect(Collectors.joining(",")) + "]";
  }

  @Override
  public TestItem get() {
    return this.testItem;
  }
}
