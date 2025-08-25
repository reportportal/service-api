package com.epam.ta.reportportal.util;

import com.epam.reportportal.rules.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControllerUtilsTest {

  @Test
  void safeParseLong_nullValue_throwsException() {
    assertThrows(ReportPortalException.class, () -> ControllerUtils.safeParseLong(null));
  }

  @Test
  void safeParseInt_nullValue_throwsException() {
    assertThrows(ReportPortalException.class, () -> ControllerUtils.safeParseInt(null));
  }
}
