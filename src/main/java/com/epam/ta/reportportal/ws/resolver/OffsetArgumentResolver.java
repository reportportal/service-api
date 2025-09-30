/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.resolver;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static java.util.stream.Collectors.toList;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.querygen.CriteriaHolder;
import com.epam.ta.reportportal.commons.querygen.FilterTarget;
import com.epam.ta.reportportal.util.OffsetRequest;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} for ReportPortal OffsetRequest with enhanced sort resolution.
 *
 * @author Andrei Varabyeu
 */
@Component
public class OffsetArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String OFFSET_PARAMETER = "offset";
  private static final String LIMIT_PARAMETER = "limit";
  private static final String SORT_PARAMETER = "sort";
  private static final String SORT_DELIMITER = ",";

  static final int DEFAULT_LIMIT = 100;
  static final long DEFAULT_OFFSET = 0;

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(OffsetRequest.class)
        && null != methodParameter.getParameterAnnotation(PagingOffset.class);
  }

  @Override
  public Object resolveArgument(MethodParameter methodParameter,
      ModelAndViewContainer paramModelAndViewContainer,
      NativeWebRequest webRequest, WebDataBinderFactory paramWebDataBinderFactory) {

    // Resolve offset and limit from query parameters
    String offsetParam = webRequest.getParameter(OFFSET_PARAMETER);
    String limitParam = webRequest.getParameter(LIMIT_PARAMETER);
    String[] sortParams = webRequest.getParameterValues(SORT_PARAMETER);

    long offset = resolveOffset(offsetParam);
    int limit = resolveLimit(limitParam);

    // Get domain class from annotation for enhanced sort resolution
    var pagingOffset = methodParameter.getParameterAnnotation(PagingOffset.class);
    Class<?> domainClass = null;

    if (pagingOffset != null) {
      Class<?> sortableClass = pagingOffset.sortable();
      // Only use domain class if it's not the default void.class
      if (sortableClass != void.class) {
        domainClass = sortableClass;
      }
    }

    // Use local sort resolution for consistent sort handling
    Sort sort = resolveSort(sortParams, domainClass);

    return new OffsetRequest(offset, limit, sort);
  }

  private long resolveOffset(String offsetParam) {
    if (StringUtils.hasText(offsetParam)) {
      try {
        return Long.parseLong(offsetParam);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("Invalid offset parameter: " + offsetParam, ex);
      }
    }
    return DEFAULT_OFFSET;
  }

  private int resolveLimit(String limitParam) {
    if (StringUtils.hasText(limitParam)) {
      try {
        return Integer.parseInt(limitParam);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("Invalid limit parameter: " + limitParam, ex);
      }
    }
    return DEFAULT_LIMIT;
  }

  /**
   * Resolves sort parameters and applies domain model mapping if domain class is provided.
   *
   * @param sortParams array of sort parameters in format "field" or "field,direction"
   * @param domainClass optional domain class for field mapping validation (null means no domain validation)
   * @return resolved Sort object
   */
  private Sort resolveSort(String[] sortParams, Class<?> domainClass) {
    if (domainClass == null) {
      return Sort.unsorted();
    }
    if (sortParams == null || sortParams.length == 0) {
      return Sort.by(CRITERIA_ID);
    }

    var orders = new ArrayList<Sort.Order>();

    for (String sortParam : sortParams) {
      if (StringUtils.hasText(sortParam)) {
        var order = parseSortParameter(sortParam);
        if (order != null) {
          orders.add(order);
        }
      }
    }

    var result = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);

    result = result.and(Sort.by(CRITERIA_ID));
    result = applyDomainModelMapping(result, domainClass);

    return result;
  }

  /**
   * Parses a single sort parameter string.
   *
   * @param sortParam sort parameter in format "field" or "field,direction"
   * @return Sort.Order object or null if parsing fails
   */
  private Sort.Order parseSortParameter(String sortParam) {
    try {
      String[] parts = sortParam.split(SORT_DELIMITER);

      if (parts.length == 0 || !StringUtils.hasText(parts[0])) {
        return null;
      }

      String property = parts[0].trim();

      // If only field is specified, use ASC by default
      if (parts.length == 1) {
        return Sort.Order.asc(property);
      }

      // If field and direction are specified
      String direction = parts[1].trim().toLowerCase();
      return switch (direction) {
        case "desc", "descending" -> Sort.Order.desc(property);
        case "asc", "ascending" -> Sort.Order.asc(property);
        default -> throw new IllegalArgumentException("Invalid sort direction: " + direction +
            ". Valid values are: asc, desc, ascending, descending");
      };
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid sort parameter format: " + sortParam +
          ". Expected format: 'field' or 'field,direction'", ex);
    }
  }

  /**
   * Applies domain model mapping to sort orders, validating field names against FilterTarget.
   *
   * @param sort original sort object
   * @param domainModelType domain model class for validation
   * @return mapped sort object
   */
  private Sort applyDomainModelMapping(Sort sort, Class<?> domainModelType) {
    FilterTarget filterTarget = FilterTarget.findByClass(domainModelType);

    return Sort.by(StreamSupport.stream(sort.spliterator(), false).map(order -> {
      Optional<CriteriaHolder> criteriaHolder = filterTarget.getCriteriaByFilter(
          order.getProperty());
      BusinessRule
          .expect(criteriaHolder, Preconditions.IS_PRESENT)
          .verify(ErrorType.INCORRECT_SORTING_PARAMETERS, order.getProperty());
      return new Sort.Order(order.getDirection(), order.getProperty());
    }).collect(toList()));
  }
}
