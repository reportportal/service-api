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

package com.epam.reportportal.infrastructure.persistence.commons.querygen.query;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.GroupField;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.TableLike;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class QuerySupplier implements Supplier<SelectQuery<? extends Record>> {

  private final SelectQuery<? extends Record> selectQuery;
  private final List<JoinEntity> joinEntities;

  public QuerySupplier(SelectQuery<? extends Record> selectQuery, List<JoinEntity> joinEntities) {
    this.selectQuery = selectQuery;
    this.joinEntities = joinEntities;
  }

  public QuerySupplier(SelectQuery<? extends Record> selectQuery) {
    this.selectQuery = selectQuery;
    this.joinEntities = Lists.newArrayList();
  }

  @Override
  public SelectQuery<? extends Record> get() {
    joinEntities.forEach(
        join -> selectQuery.addJoin(join.getTable(), join.getJoinType(), join.getJoinCondition()));
    return selectQuery;
  }

  public QuerySupplier addJoin(TableLike<?> table, JoinType joinType, Condition condition) {
    addJoinToEnd(JoinEntity.of(table, joinType, condition));
    return this;
  }

  public QuerySupplier addJoinToStart(JoinEntity joinEntity) {
    joinEntities.add(0, joinEntity);
    return this;
  }

  public QuerySupplier addJoinToEnd(JoinEntity joinEntity) {
    joinEntities.add(joinEntity);
    return this;
  }

  public boolean addJoin(int index, JoinEntity joinEntity) {
    if (index >= 0 && index <= joinEntities.size()) {
      joinEntities.add(index, joinEntity);
      return true;
    } else {
      return false;
    }

  }

  public QuerySupplier addSelect(Field<?> as) {
    selectQuery.addSelect(as);
    return this;
  }

  public QuerySupplier addOrderBy(SortField<?> sortField) {
    selectQuery.addOrderBy(sortField);
    return this;
  }

  public QuerySupplier addLimit(int limit) {
    selectQuery.addLimit(limit);
    return this;
  }

  public QuerySupplier addOffset(int offset) {
    selectQuery.addOffset(offset);
    return this;
  }

  public QuerySupplier addCondition(Condition condition) {
    selectQuery.addConditions(condition);
    return this;
  }

  public QuerySupplier addHaving(Condition condition) {
    selectQuery.addHaving(condition);
    return this;
  }

  public QuerySupplier addGroupBy(GroupField fields) {
    selectQuery.addGroupBy(fields);
    return this;
  }

  public QuerySupplier addGroupBy(Collection<? extends GroupField> fields) {
    selectQuery.addGroupBy(fields);
    return this;
  }
}
