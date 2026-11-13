package com.epam.reportportal.base.infrastructure.persistence.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.epam.reportportal.base.ws.BaseMvcTest;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class EncryptConfigurationTest extends BaseMvcTest {

  @Autowired
  private BasicTextEncryptor basicTextEncryptor;

  @Autowired
  private StandardPBEStringEncryptor strongTextEncryptor;

  @Test
  void basicEncryptionTest() {
    String message = "sensitive data";
    String encrypted = basicTextEncryptor.encrypt(message);
    assertNotEquals(message, encrypted);
    assertEquals(message, basicTextEncryptor.decrypt(encrypted));
  }

  @Test
  void strongEncryptionTest() {
    String message = "sensitive data";
    String encrypted = strongTextEncryptor.encrypt(message);
    assertNotEquals(message, encrypted);
    assertEquals(message, strongTextEncryptor.decrypt(encrypted));
  }
}
