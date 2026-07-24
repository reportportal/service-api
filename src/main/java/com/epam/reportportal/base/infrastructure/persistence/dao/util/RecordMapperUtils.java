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

package com.epam.reportportal.base.infrastructure.persistence.dao.util;

import java.util.Arrays;
import java.util.function.Predicate;
import org.jooq.Field;
import org.jooq.Record;

/**
 * Helpers for jOOQ record field selection predicates.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class RecordMapperUtils {

  private RecordMapperUtils() {
    //static only
  }

  public static Predicate<Field<?>> fieldExcludingPredicate(Field<?>... fields) {
    return field -> Arrays.stream(fields)
        .noneMatch(f -> f.getName().equalsIgnoreCase(field.getName()) && f.getQualifiedName()
            .toString()
            .equalsIgnoreCase(field.getQualifiedName().toString()));
  }

  /**
   * Returns {@code true} if the given jOOQ {@link Record} contains the specified {@link Field}.
   *
   * @param rec   the record to inspect; must not be {@code null}
   * @param field the field whose presence is checked; must not be {@code null}
   * @return {@code true} if the record contains the field, {@code false} otherwise
   */
  public static boolean hasField(Record rec, Field<?> field) {
    return Arrays.stream(rec.fields()).anyMatch(f -> f.equals(field));
  }

  /**
   * Returns the value of the specified typed {@link Field} from the given {@link Record}, or
   * {@code null} if the field is not present in the record.
   *
   * @param rec   the record to read from; must not be {@code null}
   * @param field the field whose value is retrieved; must not be {@code null}
   * @param <T>   the Java type of the field value
   * @return the field value, or {@code null} when the field is absent
   */
  public static <T> T getFieldValue(Record rec, Field<T> field) {
    return hasField(rec, field) ? rec.get(field) : null;
  }

  /**
   * Returns the value of the specified {@link Field} from the given {@link Record} converted to
   * {@code type}, or {@code null} if the field is not present in the record.
   *
   * @param rec   the record to read from; must not be {@code null}
   * @param field the field whose value is retrieved; must not be {@code null}
   * @param type  the target conversion type; must not be {@code null}
   * @param <T>   the Java type to convert the field value to
   * @return the converted field value, or {@code null} when the field is absent
   */
  public static <T> T getFieldValue(Record rec, Field<?> field, Class<T> type) {
    return hasField(rec, field) ? rec.get(field, type) : null;
  }
}
