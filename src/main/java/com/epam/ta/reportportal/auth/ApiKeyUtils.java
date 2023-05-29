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

package com.epam.ta.reportportal.auth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class ApiKeyUtils {

  private ApiKeyUtils() {
  }

  /**
   * Validate token sign
   */
  public static boolean validateToken(String apiKey) {
    String[] nameChecksum = apiKey.split("_", 2);
    byte[] checksumBytes = Base64.getUrlDecoder().decode(nameChecksum[1]);
    byte[] actualUuid = Arrays.copyOf(checksumBytes, 16);
    byte[] actualHash = Arrays.copyOfRange(checksumBytes, 16, checksumBytes.length);

    byte[] nameBytes = nameChecksum[0].getBytes(StandardCharsets.UTF_8);
    ByteBuffer nameUuidBb = ByteBuffer.wrap(new byte[nameBytes.length + actualUuid.length]);
    nameUuidBb.put(nameBytes);
    nameUuidBb.put(actualUuid);

    byte[] expected = DigestUtils.sha3_256(nameUuidBb.array());

    return Arrays.equals(actualHash, expected);
  }

}