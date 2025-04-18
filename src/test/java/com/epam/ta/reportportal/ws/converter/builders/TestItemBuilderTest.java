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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.reporting.ParameterResource;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemBuilderTest {

  @Test
  void testItemBuilder() {
    final Launch launch = new Launch();
    launch.setId(1L);
    launch.setName("name");
    final ParameterResource parameterResource = new ParameterResource();
    parameterResource.setKey("key");
    parameterResource.setValue("value");
    final String description = "description";
    final String typeValue = "step";

    final TestItem testItem = new TestItemBuilder().addDescription(description)
        .addType(typeValue)
        .addLaunchId(launch.getId())
        .addParameters(Collections.singletonList(parameterResource))
        .addAttributes(Sets.newHashSet(new ItemAttributesRQ("key", "value")))
        .addParentId(1L)
        .get();

    assertEquals(testItem.getLaunchId(), launch.getId());
    assertEquals(description, testItem.getDescription());
    assertEquals(TestItemTypeEnum.STEP, testItem.getType());
    final Parameter param = new Parameter();
    param.setKey("key");
    param.setValue("value");
    assertTrue(testItem.getParameters().contains(param));
    assertThat(testItem.getAttributes()).containsExactly(new ItemAttribute("key", "value", false));
    assertNotNull(testItem.getParentId());
  }

  @Test
  void addStartRqTest() {
    final StartTestItemRQ rq = new StartTestItemRQ();
    rq.setType("step");
    final ParameterResource parameterResource = new ParameterResource();
    parameterResource.setKey("key");
    parameterResource.setValue("value");
    rq.setParameters(Collections.singletonList(parameterResource));
    final String uuid = "uuid";
    rq.setUniqueId(uuid);
    final String description = "description";
    rq.setDescription(description);
    final Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    rq.setStartTime(now);
    final String name = "name";
    rq.setName(name);

    final TestItem testItem = new TestItemBuilder().addStartItemRequest(rq).get();

    assertEquals(TestItemTypeEnum.STEP, testItem.getType());
    final Parameter param = new Parameter();
    param.setKey("key");
    param.setValue("value");
    assertTrue(testItem.getParameters().contains(param));
    assertEquals(uuid, testItem.getUniqueId());
    assertEquals(description, testItem.getDescription());
    assertEquals(now, testItem.getStartTime());
    assertEquals(name, testItem.getName());
  }

  @Test
  void addResultsTest() {
    TestItem item = new TestItem();
    final Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    item.setStartTime(now);
    final TestItemResults itemResults = new TestItemResults();
    itemResults.setEndTime(now.plusSeconds(120));
    item.setItemResults(itemResults);
    final ItemAttribute systemAttribute = new ItemAttribute("key", "val", true);
    item.setAttributes(Sets.newHashSet(new ItemAttribute("key", "val", false), systemAttribute));

    final TestItem resultItem = new TestItemBuilder(item).addTestItemResults(itemResults)
        .addStatus(StatusEnum.PASSED)
        .overwriteAttributes(Sets.newHashSet(new ItemAttributeResource("k", "v")))
        .get();

    assertEquals(120, resultItem.getItemResults().getDuration(), 0.1);
    assertEquals(StatusEnum.PASSED, resultItem.getItemResults().getStatus());
    assertThat(resultItem.getAttributes()).containsExactlyInAnyOrder(systemAttribute,
        new ItemAttribute("k", "v", false));
  }

  @Test
  void providedTestCaseIdTest() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setName("item");
    request.setType("step");
    String testCaseId = "my-test-case-id";
    request.setTestCaseId(testCaseId);

    TestItem testItem = new TestItemBuilder().addStartItemRequest(request).get();

    assertEquals(testCaseId, testItem.getTestCaseId());
    assertEquals(testCaseId.hashCode(), testItem.getTestCaseHash().intValue());
  }

  @Test
  void providedTestCaseIdWith1025SymbolsTest() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setName("item");
    request.setType("step");
    String testCaseId = RandomStringUtils.random(1025, true, true);
    request.setTestCaseId(testCaseId);

    TestItem item = new TestItemBuilder().addStartItemRequest(request).get();

    assertTrue(item.getTestCaseId().length() <= 1024);
    assertEquals(testCaseId.substring(0, 1010), item.getTestCaseId().substring(0, 1010));
    assertEquals("[" + testCaseId.substring(1011).hashCode() + "]",
        item.getTestCaseId().substring(1011));
    assertEquals(testCaseId.hashCode(), item.getTestCaseHash().intValue());
  }

  @Test
  void testCaseIdGeneratedFromCodeRefTest() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setName("item");
    request.setType("step");
    String codeRef = "com.epam.ta.reportportal.core.item.identity.TestCaseIdHandlerImplTest";
    request.setCodeRef(codeRef);

    TestItem item = new TestItemBuilder().addStartItemRequest(request).get();

    assertEquals(codeRef, item.getTestCaseId());
    assertEquals(codeRef.hashCode(), item.getTestCaseHash().intValue());
  }

  @Test
  void testCaseIdGeneratedFromCodeRefAndParamsTest() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setName("item");
    request.setType("step");
    String codeRef = "com.epam.ta.reportportal.core.item.identity.TestCaseIdHandlerImplTest";
    request.setCodeRef(codeRef);
    ParameterResource param1 = new ParameterResource();
    param1.setKey("key1");
    String value1 = "value1";
    param1.setValue(value1);
    ParameterResource param2 = new ParameterResource();
    param2.setKey("key2");
    String value2 = "value2";
    param2.setValue(value2);
    ParameterResource param3 = new ParameterResource();
    param1.setKey("key3");
    request.setParameters(Lists.newArrayList(param1, param2, param3));

    TestItem item = new TestItemBuilder().addStartItemRequest(request).get();

    String expected = codeRef + "[" + value1 + "," + value2 + ",null]";
    assertEquals(expected, item.getTestCaseId());
    assertEquals(expected.hashCode(), item.getTestCaseHash().intValue());
  }
}
