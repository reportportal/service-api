/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.OffsetRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} for ReportPortal User Roles.
 *
 * @author Andrei Varabyeu
 */
public class OffsetArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String OFFSET_PARAMETER = "offset";
  private static final String LIMIT_PARAMETER = "limit";
  private static final String SORT_PARAMETER = "sort";

  static final int DEFAULT_LIMIT = 20;
  static final long DEFAULT_OFFSET = 0;

  /**
   * Returns TRUE if method argument is {@link UserRole} and annotated by {@link PagingOffset}
   * annotation
   */
  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(OffsetRequest.class)
        && null != methodParameter.getParameterAnnotation(
        PagingOffset.class);
  }


  @Override
  public Object resolveArgument(MethodParameter methodParameter,
      ModelAndViewContainer paramModelAndViewContainer,
      NativeWebRequest webRequest, WebDataBinderFactory paramWebDataBinderFactory) {
    // Resolve offset from the query parameter
    String offsetParam = webRequest.getParameter(OFFSET_PARAMETER);
    String limitParam = webRequest.getParameter(LIMIT_PARAMETER);
    String[] sortParams = webRequest.getParameterValues(SORT_PARAMETER);

    long offset = resolveOffset(offsetParam);
    int limit = resolveLimit(limitParam);
    Sort sort = sortParams == null ? Sort.unsorted() : Sort.by(sortParams);

    // Create and return an OffsetRequest object using the parsed values
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

}
