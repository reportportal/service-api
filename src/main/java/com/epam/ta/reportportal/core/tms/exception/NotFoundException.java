package com.epam.ta.reportportal.core.tms.exception;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class NotFoundException extends RuntimeException {

  public NotFoundException(final String templateMessage, final Object... params) {
    super(MessageFormat.format(templateMessage, params));
  }

  public static Supplier<NotFoundException> supplier(final String templateMessage,
      final Object... params) {
    return () -> new NotFoundException(templateMessage, params);
  }
}
