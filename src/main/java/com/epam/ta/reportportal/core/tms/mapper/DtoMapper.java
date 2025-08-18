package com.epam.ta.reportportal.core.tms.mapper;

import java.time.Instant;

public interface DtoMapper<T, V> {

  V convert(T t);

  default Long mapToEpochMilli(Instant instant) {
    return instant.toEpochMilli();
  }
}
