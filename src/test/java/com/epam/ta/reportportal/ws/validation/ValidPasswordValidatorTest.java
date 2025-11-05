package com.epam.ta.reportportal.ws.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidPasswordValidatorTest {

  private static final String SERVER_PASSWORD_MIN_LENGTH = "server.password.min.length";

  @Mock
  private ServerSettingsRepository repository;
  @Mock
  private ConstraintValidatorContext context;

  @InjectMocks
  private ValidPasswordValidator validator;

  @Test
  void isValidWhenPasswordMeetsPolicyAndMinFromDBShouldReturnTrue() {
    // given
    when(repository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.of(serverSetting("12")));
    String value = "Abcdef!1Abcdef";

    // when
    boolean result = validator.isValid(value, context);

    // then
    assertTrue(result);
  }

  @Test
  void isValidWhenNullAndAllowNullTrueShouldReturnTrue() {
    // given
    initValidator(true);

    // when
    boolean result = validator.isValid(null, context);

    // then
    assertTrue(result);
  }

  @Test
  void isValidWhenEmptyAndAllowNullTrueShouldReturnFalse() {
    // given
    initValidator(true);
    // when
    boolean result = validator.isValid("", context);
    // then
    assertFalse(result);
  }

  @Test
  void isValidWhenNullAndAllowNullFalseShouldReturnFalse() {
    // given + when
    boolean result = validator.isValid(null, context);

    // then
    assertFalse(result);
  }

  @Test
  void isValidWhenExceedsMaxShouldReturnFalse() {
    // given
    String longPassword = "A".repeat(257);

    // when
    boolean result = validator.isValid(longPassword, context);

    // then
    assertFalse(result);
  }

  @Test
  void isValidWhenBelowMinShouldReturnFalse() {
    // given
    when(repository.findByKey(SERVER_PASSWORD_MIN_LENGTH))
        .thenReturn(Optional.of(serverSetting("16")));
    String value = "Abcdef!1Abcd";

    // when
    boolean result = validator.isValid(value, context);

    // then
    assertFalse(result);
  }

  @Test
  void isValidWhenWhitespacePresentShouldReturnFalse() {
    // given + when
    boolean result = validator.isValid("Abcd ef!1", context);

    // then
    assertFalse(result);
  }

  @Test
  void isValidWhenNoDigitOrUpperOrLowerOrSpecialShouldReturnFalse() {
    assertFalse(validator.isValid("aaaaaaaa", context));
    assertFalse(validator.isValid("AAAAAAAA", context));
    assertFalse(validator.isValid("Abcdefgh", context));
    assertFalse(validator.isValid("Abcdefg1", context));
  }

  private ServerSettings serverSetting(String value) {
    ServerSettings settings = new ServerSettings();
    settings.setKey(SERVER_PASSWORD_MIN_LENGTH);
    settings.setValue(value);
    return settings;
  }

  private void initValidator(boolean allowNull) {
    validator.initialize(new ValidPassword() {
      @Override
      public String message() {
        return ValidPassword.DEFAULT_MESSAGE;
      }

      @Override
      public Class<?>[] groups() {
        return new Class[0];
      }

      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return ValidPassword.class;
      }

      @Override
      public Class<? extends jakarta.validation.Payload>[] payload() {
        return new Class[0];
      }

      @Override
      public boolean allowNull() {
        return allowNull;
      }
    });
  }

}
