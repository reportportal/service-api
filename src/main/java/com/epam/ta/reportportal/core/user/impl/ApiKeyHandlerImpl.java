/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.user.impl;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.user.ApiKeyHandler;
import com.epam.ta.reportportal.dao.ApiKeyRepository;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.user.ApiKey;
import com.epam.ta.reportportal.ws.converter.ApiKeyConverter;
import com.epam.ta.reportportal.ws.model.ApiKeyRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrei Piankouski
 */
public class ApiKeyHandlerImpl implements ApiKeyHandler {

  private static final String SECRET_KEY = "secret.key";

  private static final String DELIMITER = "delimiter";

  private static final int KEY_MIN_LENGTH = 1;

  private static final int KEY_MAX_LENGTH = 40;

  private final ApiKeyRepository apiKeyRepository;

  private final ServerSettingsRepository serverSettingsRepository;

  @Autowired
  public ApiKeyHandlerImpl(ApiKeyRepository apiKeyRepository,
      ServerSettingsRepository serverSettingsRepository) {
    this.apiKeyRepository = apiKeyRepository;
    this.serverSettingsRepository = serverSettingsRepository;
  }

  @Override
  public String createApiKey(String name, Long userId) {
    validateKeyName(name, userId);

    String apiToken = generateApiKey(name);
    String hashedApiToken = getHash(apiToken);

    ApiKey apiKey = new ApiKey();
    apiKey.setName(name);
    apiKey.setCreatedAt(LocalDateTime.now());
    apiKey.setUserId(userId);
    apiKey.setHash(hashedApiToken);

    apiKeyRepository.save(apiKey);
    return null;
  }

  @Override
  public OperationCompletionRS deleteApiKey(Long id) {
    apiKeyRepository.deleteById(id);
    return new OperationCompletionRS("Api key with ID = '" + id + "' was successfully deleted.");
  }

  @Override
  public List<ApiKeyRQ> getAllUsersApiKeys(Long userId) {
    List<ApiKey> apiKeys = apiKeyRepository.findByUserId(userId);
    return apiKeys.stream().map(ApiKeyConverter.TO_RESOURCE).collect(Collectors.toList());
  }

  private void validateKeyName(String keyName, Long userId) {
    expect(KEY_MIN_LENGTH <= keyName.length() && keyName.length() <= KEY_MAX_LENGTH, Predicates.equalTo(true)).verify(BAD_REQUEST_ERROR,
        Suppliers.formattedSupplier("API Key name should have size from {} to {} characters.", KEY_MIN_LENGTH, KEY_MAX_LENGTH)
    );
    expect(apiKeyRepository.existsByNameAndUserId(keyName, userId), Predicates.equalTo(false)).verify(BAD_REQUEST_ERROR,
        Suppliers.formattedSupplier("API Key with the same name already exists.")
    );
  }

  private String generateApiKey(String keyName) {
    StringBuilder apiKey = new StringBuilder(keyName);
    String generatedString = RandomStringUtils.random(16, true, true);
    apiKey.append(DELIMITER).append(generatedString);

    String secret = getSecret();

    String enc = getCRC32(generatedString, secret);

    apiKey.append(DELIMITER).append(enc);

    return apiKey.toString();
  }

  private String getCRC32(String generatedString, String secret) {
    ByteBuffer buffer = ByteBuffer.allocate(secret.length() + generatedString.length());
    buffer.put(secret.getBytes());
    buffer.put(generatedString.getBytes());

    CRC32 crc = new CRC32();
    crc.update(buffer.array());
    return Long.toHexString(crc.getValue());
  }

  private String getHash(String key) {
    return DigestUtils.sha256Hex(key);
  }

  private String getSecret() {
    Optional<ServerSettings> secretKey = serverSettingsRepository.findByKey(SECRET_KEY);
    return secretKey.isPresent() ? secretKey.get().getValue() : serverSettingsRepository.generateSecret();
  }
}
