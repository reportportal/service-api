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

package com.epam.ta.reportportal.core.widget.util;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class WidgetOptionUtil {

  private WidgetOptionUtil() {
    //static only
  }

  @Nullable
  public static String getValueByKey(String key, WidgetOptions widgetOptions) {

    Optional<Object> value = ofNullable(widgetOptions).flatMap(
        wo -> ofNullable(wo.getOptions()).map(options -> options.get(key)));

    value.ifPresent(
        v -> expect(v, String.class::isInstance).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
            Suppliers.formattedSupplier(
                "Wrong widget option value type for key = '{}'. String expected.", key)
        ));

    return (String) value.orElse(null);
  }

  public static <K, V> Map<K, V> getMapByKey(String key, WidgetOptions widgetOptions) {
    Optional<Object> value = ofNullable(widgetOptions).flatMap(
        wo -> ofNullable(wo.getOptions()).map(options -> options.get(key)));

    value.ifPresent(v -> expect(v, Map.class::isInstance).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
        Suppliers.formattedSupplier("Wrong widget option value type for key = '{}'. Map expected.",
            key)
    ));

    return (Map<K, V>) value.orElseGet(Collections::emptyMap);
  }

  public static boolean getBooleanByKey(String key, WidgetOptions widgetOptions) {

    return ofNullable(widgetOptions).map(
            wo -> MapUtils.isNotEmpty(wo.getOptions()) && ofNullable(wo.getOptions()
                .get(key)).map(v -> BooleanUtils.toBoolean(String.valueOf(v))).orElse(false))
        .orElse(false);
  }

  public static Optional<Integer> getIntegerByKey(String key, WidgetOptions widgetOptions) {
    return ofNullable(widgetOptions).flatMap(
        wo -> ofNullable(wo.getOptions()).map(options -> options.get(key))).map(value -> {
      try {
        return Integer.parseInt(String.valueOf(value));
      } catch (NumberFormatException ex) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier("Error during parsing integer value of key = '{}'", key)
                .get()
        );
      }
    });
  }

  public static <T> List<T> getListByKey(String key, WidgetOptions widgetOptions) {
    Optional<Object> value = ofNullable(widgetOptions).flatMap(
        wo -> ofNullable(wo.getOptions()).map(options -> options.get(key)));

    value.ifPresent(v -> expect(v, List.class::isInstance).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
        Suppliers.formattedSupplier("Wrong widget option value type for key = '{}'. List expected.",
            key)
    ));

    return (List<T>) value.orElse(Collections.emptyList());
  }
}
