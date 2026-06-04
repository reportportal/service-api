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

package com.epam.reportportal.base.infrastructure.persistence.commons;

import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.NOT_EMPTY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.REPLACE_SEPARATOR;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.TO_DATE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.TRIM_FUNCTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.normalizeId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class EntityUtilsTest {

  private static final String EXPECTED_STRING = "test";
  private static final String NORMALIZED_STRING = "strange_string";

  private static Map<Date, LocalDateTime> dates;
  private static List<String> strings;
  private static List<String> toNormalize;

  @BeforeAll
  static void setUp() throws Exception {
    dates = ImmutableMap.copyOf(
        Stream.of(1544994000000L, 1545044400000L, 739971000000L, 1539975900000L)
            .collect(Collectors.toMap(Date::new,
                it -> LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC))));
    strings = ImmutableList.of(" test", "test ", " test ", "\ntest", "test\n", "\ttest", "test\t",
        "\rtest", "test\r");
    toNormalize = ImmutableList.of("STRANGE_STRING", "StranGe_StrInG", "Strange_String",
        "Strange_strinG");
  }

  @AfterAll
  static void tearDown() throws Exception {
    dates = null;
    strings = null;
  }

  @Test
  void toLocalDateTimeTest() {
    assertNull(TO_LOCAL_DATE_TIME.apply(null));
    dates.forEach((key, value) -> assertEquals(value, TO_LOCAL_DATE_TIME.apply(key)));
  }

  @Test
  void toDateTest() {
    assertNull(TO_DATE.apply(null));
    dates.forEach((key, value) -> assertEquals(key, TO_DATE.apply(value)));
  }

  @Test
  void trimTest() {
    assertNull(TRIM_FUNCTION.apply(null));
    assertEquals("", TRIM_FUNCTION.apply(""));
    assertEquals("", TRIM_FUNCTION.apply(" "));
    assertEquals("", TRIM_FUNCTION.apply("\t"));
    assertEquals("", TRIM_FUNCTION.apply("\n"));
    assertEquals("", TRIM_FUNCTION.apply("\r"));
    strings.forEach(it -> assertEquals(EXPECTED_STRING, TRIM_FUNCTION.apply(it)));
  }

  @Test
  void notEmptyTest() {
    assertFalse(NOT_EMPTY.test(""));
    assertTrue(NOT_EMPTY.test(" "));
    assertFalse(NOT_EMPTY.test(null));
  }

  @Test
  void replaceSeparatorTest() {
    assertNull(REPLACE_SEPARATOR.apply(null));
    assertEquals("one_ two_ three", REPLACE_SEPARATOR.apply("one, two, three"));
    assertEquals("___", REPLACE_SEPARATOR.apply(",,,"));
    assertEquals("_ te_st_ _", REPLACE_SEPARATOR.apply(", te,st, ,"));
  }

  @Test
  void normalizeIdTest() {
    toNormalize.forEach(it -> assertEquals(NORMALIZED_STRING, normalizeId(it)));
  }

  @Test
  void normalizeIdFail() {
    assertThrows(NullPointerException.class, () -> normalizeId(null));
  }
}
