package com.epam.ta.reportportal.util;

import com.epam.reportportal.api.model.Offset;
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

  /**
   * Retrieves the sort fields from the given pageable object.
   *
   * @param pageable the pageable object containing sort information
   * @return a comma-separated string of sort fields, or an empty string if no sort fields are
   * present
   */
  public static String getSortFields(Pageable pageable) {
    return pageable.getSort().stream()
        .map(Order::getProperty)
        .reduce((s1, s2) -> s1 + ", " + s2)
        .orElse("");
  }

  /**
   * Retrieves the sort order direction from the given pageable object.
   *
   * @param pageable the pageable object containing sort information
   * @return the sort direction, either ASC or DESC, defaults to ASC if no sort direction is present
   */
  public static Direction getOrderEnum(Pageable pageable) {
    return pageable.getSort().stream()
        .map(Order::getDirection)
        .findFirst()
        .map(direction -> direction.equals(Direction.ASC) ? Direction.ASC : Direction.DESC)
        .orElse(Direction.ASC);
  }

}
