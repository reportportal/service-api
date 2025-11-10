/*
 * Copyright 2023 EPAM Systems
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

package com.epam.reportportal.core.events.handler.item;

import static com.epam.reportportal.core.events.handler.item.TestItemPatternAnalysisRunner.IMMEDIATE_PATTERN_ANALYSIS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.reportportal.core.analyzer.pattern.handler.proxy.ItemsPatternAnalyzeProducer;
import com.epam.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TestItemPatternAnalysisRunnerTest {

  private final ItemsPatternAnalyzeProducer itemsPatternAnalyzer = mock(ItemsPatternAnalyzeProducer.class);
  private final TestItemPatternAnalysisRunner runner = new TestItemPatternAnalysisRunner(
      itemsPatternAnalyzer);

  @Test
  void shouldNotInvokePatternAnalyzer() {
    TestItem testItem = new TestItem();
    TestItemFinishedEvent event = new TestItemFinishedEvent(testItem, 1L);
    runner.handle(event, Collections.emptyMap());
    verifyNoInteractions(itemsPatternAnalyzer);
  }

  @Test
  void shouldNotInvokeFalseFlagPatternAnalyzer() {
    TestItem testItem = new TestItem();
    testItem.setItemId(1L);
    testItem.setLaunchId(1L);
    var itemAttr = new ItemAttribute(IMMEDIATE_PATTERN_ANALYSIS, "false", true);
    var hash = new HashSet<ItemAttribute>();
    hash.add(itemAttr);
    testItem.setAttributes(hash);

    TestItemFinishedEvent event = new TestItemFinishedEvent(testItem, 1L);
    runner.handle(event, Collections.emptyMap());
    verifyNoInteractions(itemsPatternAnalyzer);
  }

  @Test
  void shouldInvokePatternAnalyzer() {
    TestItem testItem = new TestItem();
    testItem.setItemId(1L);
    testItem.setLaunchId(1L);
    testItem.setAttributes(
        Sets.newHashSet(new ItemAttribute(IMMEDIATE_PATTERN_ANALYSIS, "true", true)));
    TestItemFinishedEvent event = new TestItemFinishedEvent(testItem, 1L);
    runner.handle(event, Collections.emptyMap());

    verify(itemsPatternAnalyzer, times(1)).analyze(1L, 1L, Lists.newArrayList(1L));
  }

}
