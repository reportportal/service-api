package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/fill/launch/launch-filtering-data.sql")
public class LaunchCompositeAttributeFilteringTest extends BaseMvcTest {

  @Autowired
  private LaunchRepository launchRepository;

  @Test
  void compositeAttributeHas() {
    List<Launch> launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.HAS,
            "key1:value1,key2:value2",
            false
        )));

    assertEquals(2, launches.size());

    launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.HAS,
            "key1:value1,key2:value2,key3:value3",
            false
        )));

    assertEquals(1, launches.size());
    assertEquals(1L, launches.get(0).getId());

    launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.HAS,
            "key:semi:value:colon",
            false
        )));

    assertEquals(1, launches.size());
    assertEquals(1L, launches.get(0).getId());

    launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.HAS,
            "key:semi:value:colon,key1:value1,key2:value2,key3:value3",
            false
        )));

    assertEquals(1, launches.size());
    assertEquals(1L, launches.get(0).getId());
  }

  @Test
  void compositeAttributeHasNegative() {
    List<Launch> launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.HAS,
            "key1:value1,key2:value2",
            true
        )));

    assertEquals(0, launches.size());

    launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.HAS,
            "key1:value1,key2:value2,key3:value3",
            true
        )));

    assertEquals(1, launches.size());
    assertEquals(2L, launches.get(0).getId());
  }

  @Test
  void compositeAttributeAny() {
    List<Launch> launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.ANY,
            "key1:value1,key2:value2,key3:value3",
            false
        )));

    assertEquals(2, launches.size());

    launches = launchRepository.findByFilter(buildFilterWithConditions(
        buildCompositeAttributeCondition(Condition.ANY, "key1:value1,key3:value3",
            false
        )));

    assertEquals(2, launches.size());

    launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.ANY, "key3:value3",
            false
        )));

    assertEquals(1, launches.size());
    assertEquals(1L, launches.get(0).getId());
  }

  @Test
  void compositeAttributeAnyNegative() {
    List<Launch> launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.ANY,
            "key1:value1,key2:value2,key3:value3",
            true
        )));

    assertEquals(0, launches.size());

    launches = launchRepository.findByFilter(buildFilterWithConditions(
        buildCompositeAttributeCondition(Condition.ANY, "key1:value1,key3:value3",
            true
        )));

    assertEquals(0, launches.size());

    launches = launchRepository.findByFilter(
        buildFilterWithConditions(buildCompositeAttributeCondition(Condition.ANY, "key3:value3",
            true
        )));

    assertEquals(1, launches.size());
    assertEquals(2L, launches.get(0).getId());
  }

  private FilterCondition buildCompositeAttributeCondition(Condition condition, String value,
      boolean negative) {
    return FilterCondition.builder()
        .withCondition(condition)
        .withSearchCriteria(CRITERIA_COMPOSITE_ATTRIBUTE)
        .withValue(value)
        .withNegative(negative)
        .build();
  }

  private Filter buildFilterWithConditions(FilterCondition... conditions) {
    final Filter.FilterBuilder builder = Filter.builder().withTarget(Launch.class);
    Arrays.stream(conditions).forEach(builder::withCondition);
    return builder.build();
  }
}
