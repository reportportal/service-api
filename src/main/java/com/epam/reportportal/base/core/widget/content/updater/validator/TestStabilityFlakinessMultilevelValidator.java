/*
 * Copyright 2019 EPAM Systems
 */

package com.epam.reportportal.base.core.widget.content.updater.validator;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TestStabilityFlakinessMultilevelValidator implements MultilevelValidatorStrategy {

  public static final int MAX_LEVEL_NUMBER = 10;
  /** Max executions per logical launch (widget setting); aligned with global widget item limit (600). */
  public static final int MAX_EXECUTIONS_PER_LAUNCH = 600;

  @Override
  public void validate(List<String> contentFields, Map<Filter, Sort> filterSortMapping,
      WidgetOptions widgetOptions, String[] attributes, Map<String, String> params, int limit) {
    validateFilterSortMapping(filterSortMapping);
    validateWidgetLimit(limit);

    validateWidgetOptions(widgetOptions);
    List<String> keys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widgetOptions);
    validateAttributeKeys(keys);

    List<String> values =
        ofNullable(attributes).map(Arrays::asList).orElseGet(Collections::emptyList);

    validateAttributeValues(values);
    expect(values.size() > keys.size() + 1, equalTo(false)).verify(ErrorType.BAD_REQUEST_ERROR,
        "BreadCrumb exceeds configured keys.");
  }

  private void validateFilterSortMapping(Map<Filter, Sort> filterSortMapping) {
    expect(MapUtils.isNotEmpty(filterSortMapping), equalTo(true)).verify(
        ErrorType.BAD_REQUEST_ERROR, "Filter-Sort mapping should not be empty");
  }

  private void validateWidgetLimit(int limit) {
    expect(limit > MAX_EXECUTIONS_PER_LAUNCH || limit < 2, equalTo(false)).verify(
        ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
        "Executions per launch should have value from 2 to " + MAX_EXECUTIONS_PER_LAUNCH + "."
    );
  }

  private void validateWidgetOptions(WidgetOptions widgetOptions) {
    expect(widgetOptions, Objects::nonNull).verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
        "Widgets options not provided");
  }

  private void validateAttributeKeys(List<String> attributeKeys) {
    expect(attributeKeys, CollectionUtils::isNotEmpty).verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
        "Grouping attribute keys are required.");
    expect(attributeKeys, keys -> keys.size() <= MAX_LEVEL_NUMBER)
        .verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
            "Too many grouping keys. Maximum keys count = " + MAX_LEVEL_NUMBER
        );
    attributeKeys.forEach(cf -> expect(cf, StringUtils::isNotBlank).verify(
        ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Grouping key cannot be blank"));
  }

  private void validateAttributeValues(List<String> values) {
    values.forEach(value -> expect(value, Objects::nonNull).verify(ErrorType.BAD_REQUEST_ERROR,
        "Attribute value should be not null"));
  }
}
