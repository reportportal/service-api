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

package com.epam.ta.reportportal.commons.querygen;

import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.service.LogTypeResolver;
import java.util.List;
import org.jooq.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link LogFilterPreparator}.
 *
 * @author Evelina Sarkisian
 */
@ExtendWith(MockitoExtension.class)
class LogFilterPreparatorTest {

  private static final Long PROJECT_ID = 1L;
  private static final String LOG_LEVEL_ERROR = "ERROR";
  private static final String LOG_LEVEL_WARN = "WARN";
  private static final String LOG_LEVEL_CUSTOM = "CUSTOM_TYPE";
  private static final int ERROR_LEVEL_INT = 40000;
  private static final int WARN_LEVEL_INT = 30000;
  private static final int CUSTOM_LEVEL_INT = 25000;

  @Mock
  private LogTypeResolver logTypeResolver;

  @InjectMocks
  private LogFilterPreparator preparator;

//  @BeforeEach
//  void setUp() {
//    preparator = new LogFilterPreparator(logTypeResolver);
//  }

  @Test
  @DisplayName("Should return same filter when no log level conditions present")
  void shouldReturnSameFilterWhenNoLogLevelConditions() {
    // Given
    Filter filter = createFilterWithoutLogLevel();

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertSame(filter, result, "Should return the same filter instance");
    verify(logTypeResolver, never()).resolveLogLevelFromName(anyLong(), eq(LOG_LEVEL_ERROR));
  }

  @Test
  @DisplayName("Should transform standard log level to integer")
  void shouldTransformStandardLogLevel() {
    // Given
    Filter filter = createFilterWithLogLevel(LOG_LEVEL_ERROR);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertNotSame(filter, result, "Should return a new filter instance");
    assertEquals(1, result.getFilterConditions().size());
    
    FilterCondition condition = (FilterCondition) result.getFilterConditions().get(0);
    assertEquals(String.valueOf(ERROR_LEVEL_INT), condition.getValue());
    assertEquals(CRITERIA_LOG_LEVEL, condition.getSearchCriteria());
    verify(logTypeResolver, times(1)).resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR);
  }

  @Test
  @DisplayName("Should transform custom log type to integer")
  void shouldTransformCustomLogType() {
    // Given
    Filter filter = createFilterWithLogLevel(LOG_LEVEL_CUSTOM);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_CUSTOM))
        .thenReturn(CUSTOM_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertNotNull(result);
    FilterCondition condition = (FilterCondition) result.getFilterConditions().get(0);
    assertEquals(String.valueOf(CUSTOM_LEVEL_INT), condition.getValue());
    verify(logTypeResolver, times(1)).resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_CUSTOM);
  }

  @Test
  @DisplayName("Should preserve filter ID when transforming")
  void shouldPreserveFilterId() {
    // Given
    Long filterId = 123L;
    List<ConvertibleCondition> conditions = List.of(
        new FilterCondition(Condition.GREATER_THAN_OR_EQUALS, false, LOG_LEVEL_WARN,
            CRITERIA_LOG_LEVEL)
    );
    Filter filter = new Filter(filterId, Log.class, conditions);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_WARN))
        .thenReturn(WARN_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertEquals(filterId, result.getId());
  }

  @Test
  @DisplayName("Should create mutable list for filter conditions")
  void shouldCreateMutableList() {
    // Given
    Filter filter = createFilterWithLogLevel(LOG_LEVEL_ERROR);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    // Should be able to add conditions without UnsupportedOperationException
    result.getFilterConditions().add(
        new FilterCondition(Condition.EQUALS, false, "test", "testCriteria")
    );
    assertEquals(2, result.getFilterConditions().size());
  }

  @Test
  @DisplayName("Should transform multiple log level conditions")
  void shouldTransformMultipleLogLevelConditions() {
    // Given
    List<ConvertibleCondition> conditions = List.of(
        new FilterCondition(Condition.GREATER_THAN_OR_EQUALS, false, LOG_LEVEL_ERROR,
            CRITERIA_LOG_LEVEL),
        new FilterCondition(Condition.LOWER_THAN, false, LOG_LEVEL_WARN, CRITERIA_LOG_LEVEL)
    );
    Filter filter = new Filter(Log.class, conditions);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_WARN))
        .thenReturn(WARN_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertEquals(2, result.getFilterConditions().size());
    FilterCondition firstCondition = (FilterCondition) result.getFilterConditions().get(0);
    FilterCondition secondCondition = (FilterCondition) result.getFilterConditions().get(1);
    
    assertEquals(String.valueOf(ERROR_LEVEL_INT), firstCondition.getValue());
    assertEquals(String.valueOf(WARN_LEVEL_INT), secondCondition.getValue());
    verify(logTypeResolver, times(1)).resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR);
    verify(logTypeResolver, times(1)).resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_WARN);
  }

  @Test
  @DisplayName("Should preserve non-log-level conditions")
  void shouldPreserveNonLogLevelConditions() {
    // Given
    String messageValue = "test message";
    List<ConvertibleCondition> conditions = List.of(
        new FilterCondition(Condition.CONTAINS, false, messageValue, "message"),
        new FilterCondition(Condition.GREATER_THAN_OR_EQUALS, false, LOG_LEVEL_ERROR,
            CRITERIA_LOG_LEVEL)
    );
    Filter filter = new Filter(Log.class, conditions);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertEquals(2, result.getFilterConditions().size());
    FilterCondition messageCondition = (FilterCondition) result.getFilterConditions().get(0);
    assertEquals(messageValue, messageCondition.getValue());
    assertEquals("message", messageCondition.getSearchCriteria());
  }

  @Test
  @DisplayName("Should handle composite filter conditions")
  void shouldHandleCompositeFilterConditions() {
    // Given
    List<ConvertibleCondition> innerConditions = List.of(
        new FilterCondition(Condition.GREATER_THAN_OR_EQUALS, false, LOG_LEVEL_ERROR,
            CRITERIA_LOG_LEVEL),
        new FilterCondition(Condition.LOWER_THAN, false, LOG_LEVEL_WARN, CRITERIA_LOG_LEVEL)
    );
    CompositeFilterCondition composite = new CompositeFilterCondition(innerConditions,
        Operator.OR);
    Filter filter = new Filter(Log.class, List.of(composite));
    
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_WARN))
        .thenReturn(WARN_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertEquals(1, result.getFilterConditions().size());
    assertTrue(result.getFilterConditions().get(0) instanceof CompositeFilterCondition);
    
    CompositeFilterCondition resultComposite =
        (CompositeFilterCondition) result.getFilterConditions().get(0);
    assertEquals(2, resultComposite.getConditions().size());
    
    FilterCondition firstCondition = (FilterCondition) resultComposite.getConditions().get(0);
    FilterCondition secondCondition = (FilterCondition) resultComposite.getConditions().get(1);
    
    assertEquals(String.valueOf(ERROR_LEVEL_INT), firstCondition.getValue());
    assertEquals(String.valueOf(WARN_LEVEL_INT), secondCondition.getValue());
  }

  @Test
  @DisplayName("Should preserve negative flag in filter condition")
  void shouldPreserveNegativeFlag() {
    // Given
    FilterCondition condition = new FilterCondition(
        Condition.EQUALS,
        true, // negative
        LOG_LEVEL_ERROR,
        CRITERIA_LOG_LEVEL
    );
    Filter filter = new Filter(Log.class, List.of(condition));
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    FilterCondition resultCondition = (FilterCondition) result.getFilterConditions().get(0);
    assertTrue(resultCondition.isNegative());
  }

  @Test
  @DisplayName("Should preserve operator in filter condition")
  void shouldPreserveOperator() {
    // Given
    FilterCondition condition = new FilterCondition(
        Condition.GREATER_THAN_OR_EQUALS,
        false,
        LOG_LEVEL_ERROR,
        CRITERIA_LOG_LEVEL
    );
    Filter filter = new Filter(Log.class, List.of(condition));
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    FilterCondition resultCondition = (FilterCondition) result.getFilterConditions().get(0);
    assertEquals(Condition.GREATER_THAN_OR_EQUALS, resultCondition.getCondition());
  }

  @Test
  @DisplayName("Should handle Queryable that is not a Filter")
  void shouldHandleQueryableThatIsNotFilter() {
    // Given
    Queryable queryable = mock(Queryable.class);

    // When
    Queryable result = preparator.prepare(queryable, PROJECT_ID);

    // Then
    assertSame(queryable, result, "Should return the same queryable instance");
    verify(logTypeResolver, never()).resolveLogLevelFromName(anyLong(), eq(LOG_LEVEL_ERROR));
  }

  @Test
  @DisplayName("Should handle Queryable that is a Filter with log level")
  void shouldHandleQueryableThatIsFilter() {
    // Given
    Filter filter = createFilterWithLogLevel(LOG_LEVEL_ERROR);
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Queryable result = preparator.prepare((Queryable) filter, PROJECT_ID);

    // Then
    assertTrue(result instanceof Filter);
    Filter resultFilter = (Filter) result;
    FilterCondition condition = (FilterCondition) resultFilter.getFilterConditions().get(0);
    assertEquals(String.valueOf(ERROR_LEVEL_INT), condition.getValue());
  }

  @Test
  @DisplayName("Should handle case-insensitive log level criteria")
  void shouldHandleCaseInsensitiveLogLevelCriteria() {
    // Given - using mixed case for the search criteria to test case-insensitive matching
    FilterCondition condition = new FilterCondition(
        Condition.GREATER_THAN_OR_EQUALS,
        false,
        LOG_LEVEL_ERROR,
        "Level" // Mixed case - should still be recognized as log level criteria
    );
    Filter filter = new Filter(Log.class, List.of(condition));
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then - should transform the value and preserve the original search criteria
    FilterCondition resultCondition = (FilterCondition) result.getFilterConditions().get(0);
    assertEquals(String.valueOf(ERROR_LEVEL_INT), resultCondition.getValue());
    assertEquals("Level", resultCondition.getSearchCriteria()); // Original criteria preserved
    verify(logTypeResolver, times(1)).resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR);
  }

  @Test
  @DisplayName("Should handle nested composite conditions")
  void shouldHandleNestedCompositeConditions() {
    // Given
    FilterCondition innerCondition = new FilterCondition(
        Condition.GREATER_THAN_OR_EQUALS,
        false,
        LOG_LEVEL_ERROR,
        CRITERIA_LOG_LEVEL
    );
    CompositeFilterCondition innerComposite = new CompositeFilterCondition(
        List.of(innerCondition),
        Operator.AND
    );
    CompositeFilterCondition outerComposite = new CompositeFilterCondition(
        List.of(innerComposite),
        Operator.OR
    );
    Filter filter = new Filter(Log.class, List.of(outerComposite));
    
    when(logTypeResolver.resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR))
        .thenReturn(ERROR_LEVEL_INT);

    // When
    Filter result = preparator.prepare(filter, PROJECT_ID);

    // Then
    assertNotNull(result);
    assertTrue(result.getFilterConditions().get(0) instanceof CompositeFilterCondition);
    verify(logTypeResolver, times(1)).resolveLogLevelFromName(PROJECT_ID, LOG_LEVEL_ERROR);
  }

  // Helper methods

  private Filter createFilterWithoutLogLevel() {
    List<ConvertibleCondition> conditions = List.of(
        new FilterCondition(Condition.CONTAINS, false, "test", "message")
    );
    return new Filter(Log.class, conditions);
  }

  private Filter createFilterWithLogLevel(String levelName) {
    List<ConvertibleCondition> conditions = List.of(
        new FilterCondition(Condition.GREATER_THAN_OR_EQUALS, false, levelName, CRITERIA_LOG_LEVEL)
    );
    return new Filter(Log.class, conditions);
  }
}

