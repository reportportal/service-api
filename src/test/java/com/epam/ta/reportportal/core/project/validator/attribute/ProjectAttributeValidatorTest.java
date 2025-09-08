package com.epam.ta.reportportal.core.project.validator.attribute;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ProjectAttributeValidator}
 *
 * @author AI Assistant
 */
@ExtendWith(MockitoExtension.class)
class ProjectAttributeValidatorTest {

  @Mock
  private DelayBoundValidator delayBoundValidator;

  private ProjectAttributeValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ProjectAttributeValidator(delayBoundValidator);
    lenient().doNothing().when(delayBoundValidator).validate(anyMap(), anyMap());
  }

  @Test
  void testVerifyValidBooleanAttributes() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED.getAttribute(), "false");
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.INDEXING_RUNNING.getAttribute(), "false");

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyBooleanAttributesCaseInsensitive() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute(), "TRUE");
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED.getAttribute(), "False");
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "True");

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyInvalidBooleanAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute(), "invalid");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyNumericBooleanAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "1");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyEmptyBooleanAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.INDEXING_RUNNING.getAttribute(), "");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyNullBooleanAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED.getAttribute(), null);

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyAllBooleanAttributesValid() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    // Test all boolean attributes
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED.getAttribute(), "false");
    newAttributes.put(ProjectAttributeEnum.INDEXING_RUNNING.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED.getAttribute(), "false");
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH.getAttribute(), "false");
    newAttributes.put(ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute(), "false");
    newAttributes.put(ProjectAttributeEnum.LARGEST_RETRY_PRIORITY.getAttribute(), "false");

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyValidAnalyzerMode() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(), 
        AnalyzeMode.CURRENT_AND_THE_SAME_NAME.getValue());

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyInvalidAnalyzerModeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(), "INVALID_MODE");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyValidPercentageAttribute() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH.getAttribute(), "80");

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyInvalidPercentageAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH.getAttribute(), "150");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyValidDelayAttributes() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute(), "86400"); // 1 day in seconds
    newAttributes.put(ProjectAttributeEnum.KEEP_LOGS.getAttribute(), "604800"); // 1 week in seconds

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
    
    verify(delayBoundValidator).validate(any(), any());
  }

  @Test
  void testVerifyForeverAliasDelayAttribute() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute(), "0"); // Forever alias

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
    
    verify(delayBoundValidator).validate(any(), any());
  }

  @Test
  void testVerifyInvalidDelayAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute(), "-1");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyNonNumericDelayAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute(), "invalid");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyIncompatibleAttributeThrowsException() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    newAttributes.put("invalid.attribute", "value");

    assertThrows(ReportPortalException.class, 
        () -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyNotificationAttributePattern() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    // Should pass - matches notification pattern
    newAttributes.put("notifications.slack.enabled", "true");
    newAttributes.put("notifications.webhook.enabled", "false");

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
  }

  @Test
  void testVerifyMixedValidAttributes() {
    Map<String, String> currentAttributes = new HashMap<>();
    Map<String, String> newAttributes = new HashMap<>();
    
    // Mix of valid boolean, analyzer mode, percentage, and delay attributes
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), "true");
    newAttributes.put(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(), 
        AnalyzeMode.CURRENT_AND_THE_SAME_NAME.getValue());
    newAttributes.put(ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH.getAttribute(), "95");
    newAttributes.put(ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute(), "7776000"); // 90 days
    newAttributes.put("notifications.email.enabled", "false");

    assertDoesNotThrow(() -> validator.verifyProjectAttributes(currentAttributes, newAttributes));
    
    verify(delayBoundValidator).validate(any(), any());
  }
}