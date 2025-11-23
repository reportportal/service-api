package com.epam.reportportal.util;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@UtilityClass
public class PageableUtils {

  /**
   * Loads data page by page from specified {@code producer}
   * <p>
   * In first call of {@code producer} it's trying to load all data in one time, if returned page
   * will have next slice then will execute {@code producer} until all data loaded
   * <p>
   * Note: exception from {@code producer} will stop execution
   *
   * @param producer of data in pages
   * @param <T>
   * @return received data from producer or empty list if no content returned
   */
  public static <T> List<T> loadAll(Function<Pageable, Page<T>> producer) {
    requireNonNull(producer);

    var first = PageRequest.of(0, 100);

    var currentPage = producer.apply(first);

    if (Objects.isNull(currentPage)) {
      return null;
    }

    var data = new ArrayList<>(currentPage.getContent());

    while (currentPage.hasNext()) {
      currentPage = producer.apply(currentPage.nextPageable());
      data.addAll(currentPage.getContent());
    }

    return data;
  }

}
