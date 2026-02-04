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

package com.epam.reportportal.base.infrastructure.persistence.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Dzianis_Shybeka
 */
class DataEncoderTest {

  private DataEncoder encoder;

  @BeforeEach
  void setUp() {
    encoder = new DataEncoder();
  }

  @Test
  void encode_decode_with_empty_input() throws Exception {
    //  given:
    String input = "";

    //  when:
    String encoded = encoder.encode(input);

    //  then:
    assertTrue(encoded.isEmpty());

    //	and:

    //	when:
    String decoded = encoder.decode(encoded);

    //	then:
    assertTrue(decoded.isEmpty());
  }

  @Test
  void encode_decode_with_null_input() throws Exception {
    //  given:
    String input = null;

    //  when:
    String encoded = encoder.encode(input);

    //  then:
    assertNull(encoded);

    //	and:

    //	when:
    String decoded = encoder.decode(encoded);

    //	then:
    assertNull(decoded);
  }

  @Test
  void encode_decode() throws Exception {
    //  given:
    String input = "/data/path/file.ext";

    //  when:
    String encoded = encoder.encode(input);

    //  then:
    assertFalse(encoded.isEmpty());

    //	and:

    //	when:
    String decoded = encoder.decode(encoded);

    //	then:
    assertFalse(decoded.isEmpty());
    assertEquals(input, decoded);
  }
}
