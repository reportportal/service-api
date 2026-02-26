
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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

import org.jooq.Field;
import org.jooq.TableField;

/**
 * Transforms string into jooq {@link Field} representation
 *
 * @author Ivan Budaev
 */
public final class JooqFieldNameTransformer {

  public static Field<?> fieldName(TableField tableField) {
    return field(name(tableField.getName()));
  }

  public static Field<?> fieldName(String tableFieldName) {
    return field(name(tableFieldName));
  }

  public static Field<?> fieldName(String... fieldQualifiers) {
    return field(name(fieldQualifiers));
  }
}
