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

package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_INDEX;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_LOG_SEARCH;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_PRIORITY;
import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.DOES_SUPPORT_SEARCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
class AnalyzerUtilsTest {

  @Test
  void testParsing() {
    ExchangeInfo mock = mock(ExchangeInfo.class);
    when(mock.getArguments()).thenReturn(
        ImmutableMap.<String, Object>builder().put(ANALYZER_PRIORITY, 1)
            .put(ANALYZER_INDEX, true)
            .build());
    assertEquals(1, AnalyzerUtils.EXCHANGE_PRIORITY.applyAsInt(mock));
    assertTrue(AnalyzerUtils.DOES_SUPPORT_INDEX.test(mock));
  }

  @Test
  void testDefaultValues() {
    ExchangeInfo mock = mock(ExchangeInfo.class);
    when(mock.getArguments()).thenReturn(Collections.emptyMap());
    assertEquals(Integer.MAX_VALUE, AnalyzerUtils.EXCHANGE_PRIORITY.applyAsInt(mock));
    assertFalse(AnalyzerUtils.DOES_SUPPORT_INDEX.test(mock));
  }

  @Test
  void testBadValues() {
    ExchangeInfo mock = mock(ExchangeInfo.class);
    when(mock.getArguments()).thenReturn(
        ImmutableMap.<String, Object>builder().put(ANALYZER_PRIORITY, "abracadabra")
            .put(ANALYZER_INDEX, "666")
            .put(ANALYZER_LOG_SEARCH, "666")
            .build());
    assertEquals(Integer.MAX_VALUE, AnalyzerUtils.EXCHANGE_PRIORITY.applyAsInt(mock));
    assertFalse(AnalyzerUtils.DOES_SUPPORT_INDEX.test(mock));
    assertFalse(DOES_SUPPORT_SEARCH.test(mock));
  }
}