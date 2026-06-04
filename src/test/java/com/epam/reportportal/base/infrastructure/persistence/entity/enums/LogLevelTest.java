/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogLevelTest {

  private Map<LogLevel, List<String>> allowedNames;
  private List<String> disallowedNames;
  private Map<LogLevel, Integer> allowedCodes;
  private List<Integer> disallowedCodes;

  @BeforeEach
  void setUp() {
    allowedNames = Arrays.stream(LogLevel.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.name(), it.name().toUpperCase(), it.name().toLowerCase())));
    disallowedNames = Arrays.asList("NoSuchLogLevel", "", " ", "null", "warrrn");
    allowedCodes = Arrays.stream(LogLevel.values())
        .collect(Collectors.toMap(it -> it, LogLevel::toInt));
    disallowedCodes = Arrays.asList(0, 1500, 999, 7);
  }

  @Test
  void isGreaterOrEqual() {
    final LogLevel warn = LogLevel.WARN;
    final LogLevel trace = LogLevel.TRACE;
    final LogLevel warnSecond = LogLevel.WARN;
    final LogLevel fatal = LogLevel.FATAL;

    assertTrue(warn.isGreaterOrEqual(trace));
    assertTrue(warn.isGreaterOrEqual(warnSecond));
    assertFalse(warn.isGreaterOrEqual(fatal));
  }

  @Test
  void toLevel() {
    allowedNames.forEach((key, value) -> value.forEach(val -> {
      final Optional<LogLevel> optional = LogLevel.toLevel(val);
      assertTrue(optional.isPresent());
      assertEquals(key, optional.get());
    }));
    disallowedNames.forEach(it -> assertFalse(LogLevel.toLevel(it).isPresent()));
  }

  @Test
  void toLevelInt() {
    allowedCodes
        .forEach((key, value) -> assertEquals(key.toString(), LogLevel.toLevel(value).get()));
  }

  @Test
  void toCustomLogLevel() {
    allowedNames.forEach((key, value) -> value.forEach(val ->
        assertEquals(key.toInt(), LogLevel.toCustomLogLevel(val).get())));
    allowedCodes.forEach((key, val) ->
        assertFalse(LogLevel.toCustomLogLevel(val.toString()).isPresent()));
    disallowedCodes.forEach(it ->
        assertFalse(LogLevel.toCustomLogLevel(it.toString()).isPresent()));
  }

  @Test
  void toCustomLogLevelNames() {
    Collections.shuffle(disallowedNames);
    final String wrongLogName = disallowedNames.get(0);
    assertFalse(LogLevel.toCustomLogLevel(wrongLogName).isPresent());
  }

  @Test
  void toCustomLogLevelCodesFail() {
    assertFalse(LogLevel.toCustomLogLevel(disallowedCodes.get(0).toString()).isPresent());
  }

  @Test
  void toLevelIntFail() {
    Collections.shuffle(disallowedCodes);
    final Integer code = disallowedCodes.get(0);

    Optional<String> level = LogLevel.toLevel(code);
    assertFalse(level.isPresent());
  }
}
