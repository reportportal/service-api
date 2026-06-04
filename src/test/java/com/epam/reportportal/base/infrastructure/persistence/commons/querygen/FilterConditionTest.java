package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;


/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class FilterConditionTest {

  @Test
  public void testInBuilder() {
    FilterCondition condition = FilterCondition.builder()
        .in("criteria", Lists.newArrayList(1, 2, 3)).build();
    assertEquals("criteria", condition.getSearchCriteria());
    assertEquals(Condition.IN, condition.getCondition());
    assertEquals("1,2,3", condition.getValue());
  }

  @Test
  public void testEqBuilder() {
    FilterCondition condition = FilterCondition.builder().eq("criteria", "value").build();
    assertEquals("criteria", condition.getSearchCriteria());
    assertEquals(Condition.EQUALS, condition.getCondition());
    assertEquals("value", condition.getValue());
  }

}
