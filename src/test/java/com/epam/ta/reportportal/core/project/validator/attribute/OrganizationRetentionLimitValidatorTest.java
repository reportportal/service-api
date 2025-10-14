package com.epam.ta.reportportal.core.project.validator.attribute;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.organization.settings.OrganizationRetentionPolicyHandler;
import com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum;
import com.epam.ta.reportportal.dao.organization.OrganizationSettingsRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationSetting;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationRetentionLimitValidatorTest {

  @Mock
  private OrganizationSettingsRepository settingsRepository;

  private OrganizationRetentionLimitValidator validator;

  private final Long orgId = 1L;

  @BeforeEach
  void setUp() {
    validator = new OrganizationRetentionLimitValidator(settingsRepository, new OrganizationRetentionPolicyHandler());
  }

  @Test
  void validateWhenAttributesNullShouldDoNothing() {
    // Given
    Map<String, String> attrs = null;

    // When + Then
    assertDoesNotThrow(() -> validator.validate(orgId, attrs));
    verifyNoInteractions(settingsRepository);
  }

  @Test
  void validateWhenAttributesEmptyShouldDoNothing() {
    // Given
    Map<String, String> attrs = Map.of();

    // When + Then
    assertDoesNotThrow(() -> validator.validate(orgId, attrs));
    verifyNoInteractions(settingsRepository);
  }

  @Test
  void validateWhenProjectLogsExceedsOrgLimitShouldThrowBadRequest() {
    // Given
    List<OrganizationSetting> settings = settings(0, 10, 0);
    when(settingsRepository.findByOrganizationId(orgId)).thenReturn(settings);
    Map<String, String> attrs = Map.of("job.keepLogs", String.valueOf(11 * 24 * 3600L));

    // When + Then
    assertThrows(ReportPortalException.class, () -> validator.validate(orgId, attrs));
  }

  @Test
  void validateWhenProjectLaunchesEqualsOrgLimitShouldPass() {
    // Given
    List<OrganizationSetting> settings = settings(5 * 24 * 3600, 0, 0);
    when(settingsRepository.findByOrganizationId(orgId)).thenReturn(settings);
    Map<String, String> attrs = Map.of("job.keepLaunches", String.valueOf(5 * 24 * 3600L));

    // When + Then
    assertDoesNotThrow(() -> validator.validate(orgId, attrs));
  }

  @Test
  void validateWhenProjectAttachmentsLessThanOrgLimitShouldPass() {
    // Given
    List<OrganizationSetting> settings = settings(0, 0, 7 * 24 * 3600);
    when(settingsRepository.findByOrganizationId(orgId)).thenReturn(settings);
    Map<String, String> attrs = Map.of("job.keepScreenshots", String.valueOf(3 * 24 * 3600L));

    // When + Then
    assertDoesNotThrow(() -> validator.validate(orgId, attrs));
  }

  @Test
  void validateWhenOrgLogsUnlimitedShouldPassForAnyProjectValue() {
    // Given
    List<OrganizationSetting> settings = settings(0, 0, 0);
    when(settingsRepository.findByOrganizationId(orgId)).thenReturn(settings);
    Map<String, String> attrs = Map.of("job.keepLogs", String.valueOf(1000L));

    // When + Then
    assertDoesNotThrow(() -> validator.validate(orgId, attrs));
  }

  @Test
  void validateWhenProjectLaunchesUnlimitedButOrgFiniteShouldThrowBadRequest() {
    // Given
    List<OrganizationSetting> settings = settings(1, 0, 0);
    when(settingsRepository.findByOrganizationId(orgId)).thenReturn(settings);
    Map<String, String> attrs = Map.of("job.keepLaunches", "0");

    // When + Then
    assertThrows(ReportPortalException.class, () -> validator.validate(orgId, attrs));
  }

  @Test
  void validateWhenProjectLaunchesUnlimitedAndOrgUnlimitedShouldPass() {
    // Given
    List<OrganizationSetting> settings = settings(0, 0, 0);
    when(settingsRepository.findByOrganizationId(orgId)).thenReturn(settings);
    Map<String, String> attrs = Map.of("job.keepLaunches", "0");

    // When + Then
    assertDoesNotThrow(() -> validator.validate(orgId, attrs));
  }

  private List<OrganizationSetting> settings(int launchesSeconds, int logsSeconds, int attachmentsSeconds) {
    OrganizationSetting launches = new OrganizationSetting();
    launches.setSettingKey(OrganizationSettingsEnum.RETENTION_LAUNCHES.getName());
    launches.setSettingValue(String.valueOf(launchesSeconds));

    OrganizationSetting logs = new OrganizationSetting();
    logs.setSettingKey(OrganizationSettingsEnum.RETENTION_LOGS.getName());
    logs.setSettingValue(String.valueOf(logsSeconds));

    OrganizationSetting attachments = new OrganizationSetting();
    attachments.setSettingKey(OrganizationSettingsEnum.RETENTION_ATTACHMENTS.getName());
    attachments.setSettingValue(String.valueOf(attachmentsSeconds));

    return List.of(launches, logs, attachments);
  }
}
