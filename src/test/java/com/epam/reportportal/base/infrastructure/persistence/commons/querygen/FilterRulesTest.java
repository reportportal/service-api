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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class FilterRulesTest {

  @Test
  void filterForString() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", String.class);
    assertTrue(FilterRules.filterForString().test(criteriaHolder));
  }

  @Test
  void filterForStringNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", Number.class);
    assertFalse(FilterRules.filterForString().test(criteriaHolder));
  }

  @Test
  void filterForBoolean() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", Boolean.class);
    assertTrue(FilterRules.filterForBoolean().test(criteriaHolder));
  }

  @Test
  void filterForBooleanNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", String.class);
    assertFalse(FilterRules.filterForBoolean().test(criteriaHolder));
  }

  @Test
  void filterForLogLevel() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", LogLevel.class);
    assertTrue(FilterRules.filterForLogLevel().test(criteriaHolder));
  }

  @Test
  void filterForLogLevelNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", Number.class);
    assertFalse(FilterRules.filterForLogLevel().test(criteriaHolder));
  }

  @Test
  void filterForNumbers() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", Long.class);
    assertTrue(FilterRules.filterForNumbers().test(criteriaHolder));
  }

  @Test
  void filterForNumbersNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", String.class);
    assertFalse(FilterRules.filterForNumbers().test(criteriaHolder));
  }

  @Test
  void filterForLtree() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("path", "string", String.class);
    assertTrue(FilterRules.filterForLtree().test(criteriaHolder));
  }

  @Test
  void filterForLtreeNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("notPath", "string", String.class);
    assertFalse(FilterRules.filterForLtree().test(criteriaHolder));
  }

  @Test
  void filterForCollections() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("path", "string", List.class);
    assertTrue(FilterRules.filterForCollections().test(criteriaHolder));
  }

  @Test
  void filterForCollectionsNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("path", "string", String.class);
    assertFalse(FilterRules.filterForCollections().test(criteriaHolder));
  }

  @Test
  void filterForAggregation() {
    CriteriaHolder criteriaHolder = new CriteriaHolderBuilder().newBuilder("string", "string",
            String.class)
        .withAggregateCriteria("array_agg(string)")
        .get();
    assertTrue(FilterRules.filterForArrayAggregation().test(criteriaHolder));
  }

  @Test
  void filterForAggregationNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolderBuilder().newBuilder("string", "string",
        String.class).get();
    assertFalse(FilterRules.filterForArrayAggregation().test(criteriaHolder));
  }

  @Test
  void numberIsPositive() {
    assertTrue(FilterRules.numberIsPositive().test(2));
  }

  @Test
  void numberIsPositiveNegative() {
    assertFalse(FilterRules.numberIsPositive().test(-1));
  }

  @Test
  void number() {
    assertTrue(FilterRules.number().test("2"));
  }

  @Test
  void numberNegative() {
    assertFalse(FilterRules.number().test("abc"));
  }

  @Test
  void dateInMillis() {
    assertTrue(FilterRules.dateInMillis().test("12345"));
  }

  @Test
  void dateInMillisNegative() {
    assertFalse(FilterRules.dateInMillis().test("abc"));
  }

  @Test
  void filterForDates() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", Date.class);
    assertTrue(FilterRules.filterForDates().test(criteriaHolder));
  }

  @Test
  void filterForDatesNegative() {
    CriteriaHolder criteriaHolder = new CriteriaHolder("string", "string", Long.class);
    assertFalse(FilterRules.filterForDates().test(criteriaHolder));
  }

  @Test
  void countOfValues() {
    String[] strings = new String[2];
    strings[0] = "str";
    strings[1] = "str";
    assertTrue(FilterRules.countOfValues(2).test(strings));
  }

  @Test
  void countOfValuesNegative() {
    String[] strings = new String[2];
    strings[0] = "str";
    strings[1] = "str";
    assertFalse(FilterRules.countOfValues(3).test(strings));
  }

  @Test
  void zoneOffset() {
    assertTrue(FilterRules.zoneOffset().test("+0100"));
  }

  @Test
  void zoneOffsetNegative() {
    assertFalse(FilterRules.zoneOffset().test("+abc"));
  }

  @Test
  void timeStamp() {
    assertTrue(FilterRules.timeStamp().test("1550760664"));
  }

  @Test
  void timeStampNegative() {
    assertFalse(FilterRules.timeStamp().test("abc"));
  }
}
