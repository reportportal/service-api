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
import com.epam.ta.reportportal.entity.user.ApiKey;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.ws.converter.converters.ApiKeyConverter;
import com.epam.ta.reportportal.ws.model.ApiKeyRS;
import com.epam.ta.reportportal.ws.model.ApiKeysRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.annotations.VisibleForTesting;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrei Piankouski
 */
@Service
@Transactional
public class ApiKeyHandlerImpl implements ApiKeyHandler {

  private static final String DELIMITER = "_";

  private static final String FORBIDDEN_SYMBOLS_PATTERN = "[ _]";
  private static final String REPLACE_PATTERN = "-";

  private static final int KEY_MIN_LENGTH = 1;

  private static final int KEY_MAX_LENGTH = 40;

  private final ApiKeyRepository apiKeyRepository;

  @Autowired
  public ApiKeyHandlerImpl(ApiKeyRepository apiKeyRepository) {
    this.apiKeyRepository = apiKeyRepository;
  }

  @Override
  public ApiKeyRS createApiKey(String name, Long userId) {
    name = name.trim();
    validateKeyName(name, userId);

    String apiToken = generateApiKey(name);
    String hashedApiToken = getHash(apiToken);

    ApiKey apiKey = new ApiKey();
    apiKey.setName(name);
    apiKey.setCreatedAt(LocalDateTime.now());
    User user = new User();
    user.setId(userId);
    apiKey.setUser(user);
    apiKey.setHash(hashedApiToken);

    apiKeyRepository.save(apiKey);
    ApiKeyRS apiKeyRS = ApiKeyConverter.TO_RESOURCE.apply(apiKey);
    apiKeyRS.setApiKey(apiToken);
    return apiKeyRS;
  }

  @Override
  public OperationCompletionRS deleteApiKey(Long id) {
    apiKeyRepository.deleteById(id);
    return new OperationCompletionRS("Api key with ID = '" + id + "' was successfully deleted.");
  }

  @Override
  public ApiKeysRS getAllUsersApiKeys(Long userId) {
    List<ApiKey> apiKeys = apiKeyRepository.findByUserId(userId);
    ApiKeysRS apiKeysRS = new ApiKeysRS();
    apiKeysRS.setApiKeys(
        apiKeys.stream().map(ApiKeyConverter.TO_RESOURCE).collect(Collectors.toList()));
    return apiKeysRS;
  }

  private void validateKeyName(String keyName, Long userId) {
    expect(KEY_MIN_LENGTH <= keyName.length() && keyName.length() <= KEY_MAX_LENGTH,
        Predicates.equalTo(true)).verify(BAD_REQUEST_ERROR,
        Suppliers.formattedSupplier("API Key name should have size from {} to {} characters.",
            KEY_MIN_LENGTH, KEY_MAX_LENGTH)
    );
    expect(apiKeyRepository.existsByNameAndUserId(keyName, userId),
        Predicates.equalTo(false)).verify(BAD_REQUEST_ERROR,
        Suppliers.formattedSupplier("API Key with the same name already exists.")
    );
  }

  @VisibleForTesting
  String generateApiKey(String keyName) {
    keyName = keyName.replaceAll(FORBIDDEN_SYMBOLS_PATTERN, REPLACE_PATTERN);
    byte[] keyBytes = keyName.getBytes(StandardCharsets.UTF_8);

    UUID uuid = UUID.randomUUID();
    byte[] uuidBytes = convertUUIDToBytes(uuid);
    byte[] keyUuidBytes = ArrayUtils.addAll(keyBytes, uuidBytes);
    byte[] hash = DigestUtils.sha3_256(keyUuidBytes);
    byte[] uuidHashBytes = ArrayUtils.addAll(uuidBytes, hash);

    return keyName + DELIMITER + Base64.getUrlEncoder().withoutPadding()
        .encodeToString(uuidHashBytes);
  }

  private String getHash(String key) {
    return new String(DigestUtils.sha3_256(key.getBytes()));
  }

  private static byte[] convertUUIDToBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }
}
