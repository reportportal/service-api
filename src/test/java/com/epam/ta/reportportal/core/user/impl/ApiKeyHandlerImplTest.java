package com.epam.ta.reportportal.core.user.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.dao.ApiKeyRepository;
import com.epam.reportportal.rules.exception.ReportPortalException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyHandlerImplTest {

  @Mock
  private ApiKeyRepository apiKeyRepository;

  @InjectMocks
  private ApiKeyHandlerImpl handler;

  @Test
  void createApiKeyNotValidName() {
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createApiKey("", 1L)
    );
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'API Key name should have size from 1 to 40 characters.'",
        exception.getMessage()
    );
  }

  @Test
  void generateApiKeyValidName() throws NoSuchAlgorithmException {
    String apikey = handler.generateApiKey("name");

    String[] nameChecksum = apikey.split("_", 2);
    byte[] checksumBytes = Base64.getUrlDecoder().decode(nameChecksum[1]);
    byte[] actualUuid = Arrays.copyOf(checksumBytes, 16);
    byte[] actualHash = Arrays.copyOfRange(checksumBytes, 16, checksumBytes.length);

    byte[] nameBytes = nameChecksum[0].getBytes(StandardCharsets.UTF_8);
    ByteBuffer nameUuidBb = ByteBuffer.wrap(new byte[nameBytes.length + actualUuid.length]);
    nameUuidBb.put(nameBytes);
    nameUuidBb.put(actualUuid);
    MessageDigest digest = MessageDigest.getInstance("SHA3-256");
    byte[] expected = digest.digest(nameUuidBb.array());
    assertArrayEquals(actualHash, expected);
  }

}