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

package com.epam.reportportal.infrastructure.persistence.commons.querygen;

import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.google.common.collect.Lists;
import java.util.List;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CompositeFilterTest {

  @Test
  void test() {

    FilterCondition condition = FilterCondition.builder()
        .withCondition(Condition.EQUALS)
        .withValue("1")
        .withSearchCriteria("name")
        .withOperator(Operator.OR)
        .build();

    FilterCondition condition1 = FilterCondition.builder()
        .withCondition(Condition.EQUALS)
        .withValue("2")
        .withSearchCriteria("name")
        .withOperator(Operator.OR)
        .build();

    FilterCondition condition2 = FilterCondition.builder()
        .withCondition(Condition.EQUALS)
        .withValue("3")
        .withSearchCriteria("name")
        .withOperator(Operator.AND)
        .build();

    FilterCondition condition3 = FilterCondition.builder()
        .withCondition(Condition.EQUALS)
        .withValue("4")
        .withSearchCriteria("name")
        .withOperator(Operator.AND)
        .build();

    CompositeFilterCondition firstCompositeCondition = new CompositeFilterCondition(
        Lists.newArrayList(condition, condition1),
        Operator.OR
    );

    System.err.println(QueryBuilder.newBuilder(
            Filter.builder().withTarget(Launch.class).withCondition(firstCompositeCondition).build())
        .build());
    System.err.println();
    System.err.println();
    System.err.println();

    CompositeFilterCondition secondCompositeCondition = new CompositeFilterCondition(
        Lists.newArrayList(condition2, condition3),
        Operator.OR
    );

    System.err.println(QueryBuilder.newBuilder(
            Filter.builder().withTarget(Launch.class).withCondition(secondCompositeCondition).build())
        .build());
    System.err.println();
    System.err.println();
    System.err.println();

    CompositeFilterCondition compositeFilterCondition1 = new CompositeFilterCondition(
        Lists.newArrayList(firstCompositeCondition,
            secondCompositeCondition
        ), Operator.OR);

    System.err.println(QueryBuilder.newBuilder(
            Filter.builder().withTarget(Launch.class).withCondition(compositeFilterCondition1).build())
        .build());
    System.err.println();
    System.err.println();
    System.err.println();

    CompositeFilterCondition compositeFilterCondition2 = new CompositeFilterCondition(
        Lists.newArrayList(firstCompositeCondition,
            secondCompositeCondition
        ), Operator.AND);

    System.err.println(QueryBuilder.newBuilder(
            Filter.builder().withTarget(Launch.class).withCondition(compositeFilterCondition2).build())
        .build());
    System.err.println();
    System.err.println();
    System.err.println();

    List<ConvertibleCondition> conditions = Lists.newArrayList(compositeFilterCondition1,
        compositeFilterCondition2);
    ConvertibleCondition filterCondition = new CompositeFilterCondition(conditions, Operator.AND);

    Filter filter = Filter.builder().withTarget(Launch.class).withCondition(filterCondition)
        .build();

    SelectQuery<? extends Record> build = QueryBuilder.newBuilder(filter).build();

    System.err.println(build);
  }
}
