package com.epam.ta.reportportal.core.widget.content.updater.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class NotPassedTestsContentValidatorTest {

  private WidgetValidatorStrategy notPassedTestsContentValidator;

  @BeforeEach
  public void setUp() {
    notPassedTestsContentValidator = new NotPassedTestsContentValidator();
  }

  @Test
  public void testValidateWithException() {
    Exception exception = assertThrows(ReportPortalException.class, () -> {
      notPassedTestsContentValidator.validate(Collections.singletonList("test"), new HashMap<>(),
          new WidgetOptions(), 5);
    });

    String expectedMessage = "Filter-Sort mapping should not be empty";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }
}