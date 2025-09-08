package com.epam.ta.reportportal.core.project.validator.attribute;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;

//TODO need refactoring - split attributes validation logic
public class ProjectAttributeValidator {

  private static String NOTIFICATION_ATTRIBUTE_PATTERN = "notifications.\\w+.enabled";

  private final DelayBoundValidator delayBoundValidator;

  public ProjectAttributeValidator(DelayBoundValidator delayBoundValidator) {
    this.delayBoundValidator = delayBoundValidator;
  }

  public void verifyProjectAttributes(Map<String, String> currentAttributes,
      Map<String, String> newAttributes) {
    Set<String> incompatibleAttributes = newAttributes.keySet()
        .stream()
        .filter(it -> !(ProjectAttributeEnum.isPresent(it) || it.matches(
            NOTIFICATION_ATTRIBUTE_PATTERN)))
        .collect(toSet());
    expect(incompatibleAttributes, Set::isEmpty).verify(BAD_REQUEST_ERROR, incompatibleAttributes);

    ofNullable(newAttributes.get(AUTO_ANALYZER_MODE.getAttribute())).ifPresent(
        analyzerMode -> expect(AnalyzeMode.fromString(
            analyzerMode), isPresent()).verify(BAD_REQUEST_ERROR, analyzerMode));

    ofNullable(newAttributes.get(
        SEARCH_LOGS_MIN_SHOULD_MATCH.getAttribute())).ifPresent(
        attr -> BusinessRule.expect(
            validatePercentage(attr),
            BooleanUtils::isTrue
        ).verify(BAD_REQUEST_ERROR, SEARCH_LOGS_MIN_SHOULD_MATCH));

    validateBooleanAttributes(newAttributes);

    final Map<ProjectAttributeEnum, Long> delays = validateDelays(newAttributes,
        List.of(KEEP_SCREENSHOTS,
            KEEP_LOGS,
            KEEP_LAUNCHES,
            INTERRUPT_JOB_TIME
        )
    );

    delayBoundValidator.validate(currentAttributes, delays);
  }

  private Map<ProjectAttributeEnum, Long> validateDelays(Map<String, String> attributes,
      List<ProjectAttributeEnum> projectAttributes) {
    return projectAttributes.stream()
        .filter(it -> attributes.containsKey(it.getAttribute()))
        .collect(Collectors.toMap(a -> a, a -> getDelay(attributes.get(a.getAttribute()))));
  }

  private Long getDelay(String value) {
    final Long delay = FOREVER_ALIAS.equals(value) ? Long.MAX_VALUE : getLong(value);
    BusinessRule.expect(delay, d -> d >= 0)
        .verify(BAD_REQUEST_ERROR, "Delay attribute value should be greater than 0");
    return delay;
  }

  private boolean validatePercentage(String value) {
    final int percent = getInt(value);
    return percent >= 0 && percent <= 100;
  }

  private Long getLong(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException exc) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, exc.getMessage());
    }
  }

  private Integer getInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException exc) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, exc.getMessage());
    }
  }

  private void validateBooleanAttributes(Map<String, String> attributes) {
    List<ProjectAttributeEnum> booleanAttributes = List.of(
        NOTIFICATIONS_ENABLED,
        NOTIFICATIONS_EMAIL_ENABLED,
        INDEXING_RUNNING,
        AUTO_PATTERN_ANALYZER_ENABLED,
        AUTO_ANALYZER_ENABLED,
        ALL_MESSAGES_SHOULD_MATCH,
        AUTO_UNIQUE_ERROR_ANALYZER_ENABLED,
        UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS,
        LARGEST_RETRY_PRIORITY
    );

    booleanAttributes.stream()
        .filter(attr -> attributes.containsKey(attr.getAttribute()))
        .forEach(attr -> {
          String value = attributes.get(attr.getAttribute());
          BusinessRule.expect(validateBoolean(value), BooleanUtils::isTrue)
              .verify(BAD_REQUEST_ERROR,
                  String.format("Attribute '%s' should have boolean value (true/false), but was: %s",
                      attr.getAttribute(), value));
        });
  }

  private boolean validateBoolean(String value) {
    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
  }
}
