package com.epam.ta.reportportal.core.events.annotations;

import static com.epam.reportportal.model.ValidationConstraints.MAX_WIDGET_LIMIT;
import static com.epam.reportportal.model.ValidationConstraints.MIN_WIDGET_LIMIT;
import static com.epam.ta.reportportal.entity.widget.WidgetType.TEST_CASE_SEARCH;

import com.epam.ta.reportportal.model.BaseEntityRQ;
import com.epam.ta.reportportal.model.widget.MaterializedWidgetType;
import com.epam.ta.reportportal.model.widget.WidgetRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class WidgetLimitRangeValidator
    implements ConstraintValidator<WidgetLimitRange, BaseEntityRQ> {

  @Override
  public boolean isValid(BaseEntityRQ value, ConstraintValidatorContext context) {
    if (value instanceof WidgetRQ) {
      WidgetRQ widgetRQ = (WidgetRQ) value;
      if (TEST_CASE_SEARCH.getType().equalsIgnoreCase(widgetRQ.getWidgetType())) {
        return true;
      }
      int limit = widgetRQ.getContentParameters().getItemsCount();
      if (Arrays.stream(MaterializedWidgetType.values())
          .anyMatch(it -> it.getType().equalsIgnoreCase(widgetRQ.getWidgetType()))) {
        return limit >= MIN_WIDGET_LIMIT;
      }
      updateValidationMessage(
          "Widget item limit size must be between " + MIN_WIDGET_LIMIT + " and "
              + MAX_WIDGET_LIMIT,
          context
      );
      return limit >= MIN_WIDGET_LIMIT && limit <= MAX_WIDGET_LIMIT;
    }
    return false;
  }

  public void updateValidationMessage(String message, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
