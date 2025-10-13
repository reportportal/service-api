package com.epam.ta.reportportal.core.project.validator.attribute;

import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.organization.settings.OrganizationRetentionPolicyHandler;
import com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum;
import com.epam.ta.reportportal.dao.organization.OrganizationSettingsRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.organization.OrganizationSetting;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Validates that requested project retention settings do not exceed the organization-level limits.
 */
@Component
public class OrganizationRetentionLimitValidator {

  private static final Map<String, OrganizationSettingsEnum> PROJECT_KEY_TO_ORG_KEY =
      Arrays.stream(OrganizationSettingsEnum.values())
          .collect(Collectors.toUnmodifiableMap(OrganizationSettingsEnum::getProjectFormatKey, it -> it));

  private final OrganizationSettingsRepository settingsRepository;
  private final OrganizationRetentionPolicyHandler retentionPolicyHandler;

  public OrganizationRetentionLimitValidator(OrganizationSettingsRepository settingsRepository,
      OrganizationRetentionPolicyHandler retentionPolicyHandler) {
    this.settingsRepository = settingsRepository;
    this.retentionPolicyHandler = retentionPolicyHandler;
  }

  public void validate(Long organizationId, Map<String, String> newAttributes) {
    if (CollectionUtils.isEmpty(newAttributes)) {
      return;
    }

    List<OrganizationSetting> orgSettings = settingsRepository.findByOrganizationId(organizationId);

    newAttributes.keySet().stream()
        .filter(PROJECT_KEY_TO_ORG_KEY::containsKey)
        .forEach(projectKey -> {
          OrganizationSettingsEnum orgKey = PROJECT_KEY_TO_ORG_KEY.get(projectKey);
          long orgLimitSeconds = orMax(retentionPolicyHandler.getRetentionValue(orgSettings, orgKey));
          if (!isUnlimited(orgLimitSeconds)) {
            long requestedSeconds = parseSecondsOrMax(newAttributes.get(projectKey));
            if (requestedSeconds > orgLimitSeconds) {
              throw new ReportPortalException(BAD_REQUEST_ERROR, Suppliers.formattedSupplier(
                  "New value for '{}' should be less or equal to organization retention = '{}'", projectKey,
                  orgLimitSeconds).get());
            }
          }
        });
  }

  private boolean isUnlimited(long seconds) {
    return seconds == Long.MAX_VALUE;
  }

  private long parseSecondsOrMax(String value) {
    if (ProjectAttributeEnum.FOREVER_ALIAS.equalsIgnoreCase(value)) {
      return Long.MAX_VALUE;
    }
    return Long.parseLong(value);
  }

  private long orMax(int seconds) {
    return seconds == 0 ? Long.MAX_VALUE : seconds;
  }
}
