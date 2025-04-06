package com.epam.ta.reportportal.core.tms.mapper;

public interface DtoMapper<T, V> {

  V convert(T t);
}
