package com.epam.reportportal.core.widget.content.updater.validator;

import static com.epam.reportportal.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class MostTimeConsumingContentValidatorTest {

  private WidgetValidatorStrategy mostTimeConsumingContentValidator;

  @BeforeEach
  public void setUp() {
    mostTimeConsumingContentValidator = new MostTimeConsumingContentValidator();
  }

  @Test
  public void testValidateWithException() {
    Exception exception = assertThrows(ReportPortalException.class, () -> {
      HashMap<Filter, Sort> filterSortMap = new HashMap<>();
      filterSortMap.put(Filter.builder()
          .withTarget(Launch.class)
          .withCondition(FilterCondition.builder().eq("id", "1").build())
          .build(), Sort.unsorted());
      Map<String, Object> params = new HashMap<>();
      params.put(LAUNCH_NAME_FIELD, "");
      WidgetOptions widgetOptions = new WidgetOptions();
      mostTimeConsumingContentValidator.validate(Collections.singletonList("test"), filterSortMap,
          widgetOptions, 5);
    });

    String expectedMessage = LAUNCH_NAME_FIELD + " should be specified for widget.";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

}
