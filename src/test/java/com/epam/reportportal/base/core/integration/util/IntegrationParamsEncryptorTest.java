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

package com.epam.reportportal.base.core.integration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.extension.SensitiveIntegrationParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntegrationParamsEncryptorTest {

  private static final String ENCRYPTED_PREFIX = "ENC::";

  @Mock
  private BasicTextEncryptor basicTextEncryptor;

  private IntegrationParamsEncryptor encryptor;

  private String sensitiveKey;
  private String nonSensitiveKey;

  @BeforeEach
  void setUp() {
    encryptor = new IntegrationParamsEncryptor(basicTextEncryptor);
    sensitiveKey = SensitiveIntegrationParam.ALL.iterator().next();
    nonSensitiveKey = sensitiveKey + "_plain_suffix";

    lenient().when(basicTextEncryptor.encrypt(anyString()))
        .thenAnswer(inv -> ENCRYPTED_PREFIX + inv.getArgument(0));
  }

  @Test
  void shouldReturnNullWhenParamsIsNull() {
    // given
    Map<String, Object> params = null;

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertNull(result);
    verify(basicTextEncryptor, never()).encrypt(anyString());
  }

  @Test
  void shouldReturnSameReferenceWhenParamsIsEmpty() {
    // given
    Map<String, Object> params = new HashMap<>();

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertSame(params, result);
    verify(basicTextEncryptor, never()).encrypt(anyString());
  }

  @Test
  void shouldEncryptSensitiveFieldAndLeaveOthersUntouched() {
    // given
    Map<String, Object> params = Map.of(
        sensitiveKey, "secret",
        nonSensitiveKey, "public-value"
    );

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertEquals(ENCRYPTED_PREFIX + "secret", result.get(sensitiveKey));
    assertEquals("public-value", result.get(nonSensitiveKey));
    verify(basicTextEncryptor).encrypt("secret");
  }

  @Test
  void shouldNotEncryptWhenSensitiveValueIsNull() {
    // given
    Map<String, Object> params = new HashMap<>();
    params.put(sensitiveKey, null);

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertTrue(result.containsKey(sensitiveKey));
    assertNull(result.get(sensitiveKey));
    verify(basicTextEncryptor, never()).encrypt(anyString());
  }

  @Test
  void shouldStringifyNonStringSensitiveValueBeforeEncrypting() {
    // given
    Map<String, Object> params = Map.of(sensitiveKey, 12345);

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertEquals(ENCRYPTED_PREFIX + "12345", result.get(sensitiveKey));
    verify(basicTextEncryptor).encrypt("12345");
  }

  @Test
  void shouldEncryptSensitiveFieldsInsideNestedMap() {
    // given
    Map<String, Object> nested = Map.of(sensitiveKey, "nested-secret");
    Map<String, Object> params = Map.of("config", nested);

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    Map<String, Object> resultNested;
    resultNested =
        assertInstanceOf(Map.class, result.get("config")) instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    assertEquals(ENCRYPTED_PREFIX + "nested-secret", resultNested.get(sensitiveKey));
  }

  @Test
  void shouldEncryptSensitiveFieldsInsideMapsWithinList() {
    // given
    Map<String, Object> item1 = Map.of(sensitiveKey, "s1", nonSensitiveKey, "p1");
    Map<String, Object> item2 = Map.of(sensitiveKey, "s2");
    Map<String, Object> params = Map.of("items", List.of(item1, item2));

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    List<?> resultItems = assertInstanceOf(List.class, result.get("items"));
    assertEquals(2, resultItems.size());

    Map<String, Object> first = (Map<String, Object>) (Map<?, ?>) Assertions.assertInstanceOf(Map.class,
        resultItems.get(0));
    assertEquals(ENCRYPTED_PREFIX + "s1", first.get(sensitiveKey));
    assertEquals("p1", first.get(nonSensitiveKey));

    Map<String, Object> second = (Map<String, Object>) (Map<?, ?>) Assertions.assertInstanceOf(Map.class,
        resultItems.get(1));
    assertEquals(ENCRYPTED_PREFIX + "s2", second.get(sensitiveKey));
  }

  @Test
  void shouldTraverseDeeplyNestedListsOfLists() {
    // given
    Map<String, Object> deepSecret = Map.of(sensitiveKey, "deep");
    Map<String, Object> params = Map.of("matrix", List.of(List.of(List.of(deepSecret))));

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    List<?> level1 = assertInstanceOf(List.class, result.get("matrix"));
    List<?> level2 = assertInstanceOf(List.class, level1.get(0));
    List<?> level3 = assertInstanceOf(List.class, level2.get(0));
    Map<String, Object> leaf = (Map<String, Object>) (Map<?, ?>) Assertions.assertInstanceOf(Map.class, level3.get(0));
    assertEquals(ENCRYPTED_PREFIX + "deep", leaf.get(sensitiveKey));
  }

  @Test
  void shouldLeaveListOfPrimitivesUnchanged() {
    // given
    List<Object> primitives = List.of("a", 1, true);
    Map<String, Object> params = Map.of("values", primitives);

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertEquals(primitives, result.get("values"));
    verify(basicTextEncryptor, never()).encrypt(anyString());
  }

  @Test
  void shouldReturnNewMapInstanceLeavingInputUntouched() {
    // given
    Map<String, Object> params = new HashMap<>();
    params.put(sensitiveKey, "secret");

    // when
    Map<String, Object> result = encryptor.encryptSensitiveFields(params);

    // then
    assertNotSame(params, result);
    assertEquals("secret", params.get(sensitiveKey)); // original untouched
    assertEquals(ENCRYPTED_PREFIX + "secret", result.get(sensitiveKey));
  }

}
