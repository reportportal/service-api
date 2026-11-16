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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen.query;

import org.jooq.Condition;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.TableLike;

/**
 * Declares an extra jOOQ table join (type, table, and on-condition).
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class JoinEntity {

  private final TableLike<?> table;
  private final JoinType joinType;
  private final Condition joinCondition;

  private JoinEntity(TableLike<?> table, JoinType joinType, Condition joinCondition) {
    this.table = table;
    this.joinType = joinType;
    this.joinCondition = joinCondition;
  }

  public static JoinEntity of(TableLike<?> table, JoinType joinType, Condition joinCondition) {
    return new JoinEntity(table, joinType, joinCondition);
  }

  public TableLike<? extends Record> getTable() {
    return table;
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public Condition getJoinCondition() {
    return joinCondition;
  }
}
