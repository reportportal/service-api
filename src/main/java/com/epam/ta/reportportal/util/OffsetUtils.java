package com.epam.ta.reportportal.util;

import com.epam.reportportal.api.model.Offset;
import com.epam.reportportal.api.model.Offset.OrderEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public class OffsetUtils {

  private OffsetUtils() {
  }

  public static <T extends Offset> T responseWithPageParameters(T offsetObject, Pageable pageable,
      long totalElements) {
    return (T) offsetObject
        .offset((int) pageable.getOffset())
        .limit(pageable.getPageSize())
        .totalCount((int) totalElements)
        .sort(getSortFields(pageable))
        .order(getOrderEnum(pageable));
  }

  private static String getSortFields(Pageable pageable) {
    return pageable.getSort().stream()
        .map(Order::getProperty)
        .reduce((s1, s2) -> s1 + ", " + s2)
        .orElse("");
  }

  private static OrderEnum getOrderEnum(Pageable pageable) {
    return pageable.getSort().stream()
        .map(Order::getDirection)
        .findFirst()
        .map(direction -> direction.equals(Direction.ASC) ? OrderEnum.ASC : OrderEnum.DESC)
        .orElse(OrderEnum.ASC);
  }

}
