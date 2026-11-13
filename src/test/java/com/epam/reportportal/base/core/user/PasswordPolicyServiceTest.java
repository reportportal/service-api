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

package com.epam.reportportal.base.core.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceTest {

  private static final String SERVER_PASSWORD_MIN_LENGTH = "server.password.min.length";

  @Mock
  private ServerSettingsRepository serverSettingsRepository;

  @InjectMocks
  private PasswordPolicyService passwordPolicyService;

  @Test
  void isValidWhenPasswordMeetsPolicyAndMinFromDbShouldReturnTrue() {
    // given
    when(serverSettingsRepository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.of(serverSetting("12")));

    // when
    boolean result = passwordPolicyService.isValid("Abcdef!1Abcdef");

    // then
    assertThat(result).isTrue();
  }

  @Test
  void isValidWhenNullShouldReturnFalse() {
    // when
    boolean result = passwordPolicyService.isValid(null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void isValidWhenEmptyShouldReturnFalse() {
    // when
    boolean result = passwordPolicyService.isValid("");

    // then
    assertThat(result).isFalse();
  }

  @Test
  void isValidWhenExceedsMaxShouldReturnFalse() {
    // when
    boolean result = passwordPolicyService.isValid("Abc!1".concat("a".repeat(40)));

    // then
    assertThat(result).isFalse();
  }

  @Test
  void isValidWhenBelowMinFromDbShouldReturnFalse() {
    // given
    when(serverSettingsRepository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.of(serverSetting("16")));

    // when
    boolean result = passwordPolicyService.isValid("Abcdef!1Abcd");

    // then
    assertThat(result).isFalse();
  }

  @Test
  void isValidWhenSettingMissingShouldFallBackToDefaultMin() {
    // given
    when(serverSettingsRepository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.empty());

    // when
    boolean result = passwordPolicyService.isValid("Abcdef!1");

    // then
    assertThat(result).isTrue();
  }

  @Test
  void isValidWhenWhitespacePresentShouldReturnFalse() {
    // when
    boolean result = passwordPolicyService.isValid("Abcd ef!1");

    // then
    assertThat(result).isFalse();
  }

  @Test
  void isValidWhenMissingComplexityRequirementShouldReturnFalse() {
    assertThat(passwordPolicyService.isValid("aaaaaaaa")).isFalse();
    assertThat(passwordPolicyService.isValid("AAAAAAAA")).isFalse();
    assertThat(passwordPolicyService.isValid("Abcdefgh")).isFalse();
    assertThat(passwordPolicyService.isValid("Abcdefg1")).isFalse();
  }

  @Test
  void validateWhenBelowMinFromDbShouldThrow() {
    // given
    when(serverSettingsRepository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.of(serverSetting("12")));

    // when + then
    assertThatThrownBy(() -> passwordPolicyService.validate("Pass123+"))
        .isInstanceOf(ReportPortalException.class)
        .hasMessageContaining("at least 12 characters");
  }

  @Test
  void validateWhenComplexityViolatedShouldThrow() {
    // when + then
    assertThatThrownBy(() -> passwordPolicyService.validate("password"))
        .isInstanceOf(ReportPortalException.class)
        .hasMessageContaining("pattern");
  }

  @Test
  void validateWhenPasswordMeetsPolicyShouldNotThrow() {
    // given
    when(serverSettingsRepository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.of(serverSetting("12")));

    // when + then
    assertThatCode(() -> passwordPolicyService.validate("Abcdef!1Abcdef")).doesNotThrowAnyException();
  }

  private ServerSettings serverSetting(String value) {
    ServerSettings settings = new ServerSettings();
    settings.setKey(SERVER_PASSWORD_MIN_LENGTH);
    settings.setValue(value);
    return settings;
  }
}
