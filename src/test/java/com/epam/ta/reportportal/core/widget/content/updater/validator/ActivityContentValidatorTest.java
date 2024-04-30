package com.epam.ta.reportportal.core.widget.content.updater.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.reportportal.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ActivityContentValidatorTest {

  private WidgetValidatorStrategy activityContentValidator;

  @BeforeEach
  public void setUp() {
    activityContentValidator = new ActivityContentValidator();
  }

  @Test
  public void testValidateWithException() {
    Exception exception = assertThrows(ReportPortalException.class, () -> {
      activityContentValidator.validate(Collections.singletonList("test"), new HashMap<>(),
          new WidgetOptions(), 5);
    });

    String expectedMessage = "Filter-Sort mapping should not be empty";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

}
