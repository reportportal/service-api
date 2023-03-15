package com.epam.ta.reportportal.core.widget.content.updater.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class BugTrendChartContentValidatorTest {

  private WidgetValidatorStrategy bugTrendChartContentValidator;

  @BeforeEach
  public void setUp() {
    bugTrendChartContentValidator = new BugTrendChartContentValidator();
  }

  @Test
  public void testValidateWithException() {
    Exception exception = assertThrows(ReportPortalException.class, () -> {
      HashMap<Filter, Sort> filterSortMap = new HashMap<>();
      filterSortMap.put(Filter.builder()
          .withTarget(Launch.class)
          .withCondition(FilterCondition.builder().eq("id", "1").build())
          .build(), Sort.unsorted());
      bugTrendChartContentValidator.validate(
          Collections.singletonList("statistics$defects$automation_bug$total'"), filterSortMap,
          new WidgetOptions(), 5);
    });

    String expectedMessage = "Bad content fields format";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }
}