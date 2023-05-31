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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

public class ApiKeyUtils {

  private static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

  private ApiKeyUtils() {
  }

  /**
   * Validate token sign
   */
  public static boolean validateToken(String apiKey) {
    if (isUUID(apiKey)) {
      return true;
    }
    String[] nameChecksum = apiKey.split("_", 2);
    if (nameChecksum.length < 2) {
      return false;
    }
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

  private static boolean isUUID(String uuidStr) {
    Pattern pattern = Pattern.compile(UUID_PATTERN);
    Matcher matcher = pattern.matcher(uuidStr);
    return matcher.matches();
  }

}