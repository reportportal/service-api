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

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.function.Predicate;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Set of predicates which may be applied to the query builder and filter conditions<br>
 *
 * @author Andrei Varabyeu
 */
public class FilterRules {

  private FilterRules() {
    //static only
  }

  /**
   * Accepts only strings as data type
   *
   * @return Predicate
   */
  public static Predicate<CriteriaHolder> filterForString() {
    return filter -> String.class.equals(filter.getDataType());
  }

  /**
   * Accepts only strings as data type
   *
   * @return Predicate
   */
  public static Predicate<CriteriaHolder> filterForBoolean() {
    return filter -> boolean.class.equals(filter.getDataType()) || Boolean.class.isAssignableFrom(
        filter.getDataType());
  }

  public static Predicate<CriteriaHolder> filterForLogLevel() {
    return filter -> LogLevel.class.isAssignableFrom(filter.getDataType());
  }

  /**
   * Accepts numbers only numbers as data type
   *
   * @return Predicate
   */
  public static Predicate<CriteriaHolder> filterForNumbers() {
    return filter -> Number.class.isAssignableFrom(filter.getDataType());
  }

  /**
   * Accepts only 'path' criteria as a filter criteria
   */
  public static Predicate<CriteriaHolder> filterForLtree() {
    return filter -> "path".equalsIgnoreCase(filter.getFilterCriteria());
  }

  /**
   * Accepts collections as data type
   *
   * @return Predicate
   */
  public static Predicate<CriteriaHolder> filterForCollections() {
    return filter -> Collection.class.isAssignableFrom(filter.getDataType());
  }

  /**
   * Accepts filtering only for fields that are needed to be aggregated as array_agg after join
   *
   * @return Predicate
   */
  public static Predicate<CriteriaHolder> filterForArrayAggregation() {
    return filter -> filter.getAggregateCriteria().startsWith("array_agg")
        || filter.getAggregateCriteria().startsWith("array_cat");
  }

  /**
   * Accepts numbers only numbers as data type
   *
   * @return Predicate
   */
  public static <T extends Number> Predicate<T> numberIsPositive() {
    return number -> number.longValue() >= 0;
  }

  /**
   * Accepts numbers only
   *
   * @return Predicate
   */
  public static Predicate<String> number() {
    return NumberUtils::isCreatable;
  }

  /**
   * Accepts numbers only
   *
   * @return Predicate
   */
  public static Predicate<String> dateInMillis() {
    return object -> {
      /*
       * May be rewritten in future. I suppose date as long value
       */
      return number().test(object);
    };
  }

  /**
   * Accepts only dates as data type
   *
   * @return Predicate
   */
  public static Predicate<CriteriaHolder> filterForDates() {
    return filter -> Date.class.isAssignableFrom(filter.getDataType());
  }

  /**
   * Count of values provided to filter
   *
   * @return Predicate
   */
  public static Predicate<String[]> countOfValues(final int count) {
    return values -> count == values.length;
  }

  public static Predicate<String> zoneOffset() {
    return value -> {
      if (value == null) {
        return false;
      }
      try {
        ZoneOffset.of(value);
      } catch (DateTimeException e) {
        return false;
      }
      return true;
    };
  }

  public static Predicate<String> timeStamp() {
    return value -> {
      if (value == null) {
        return false;
      }
      try {
        long offset = Long.parseLong(value);
        LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)).plusMinutes(offset);
      } catch (NumberFormatException | DateTimeException e) {
        return false;
      }
      return true;
    };
  }

}
