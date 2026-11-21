/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.marketplace;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Helpers shared by everything that downloads a plugin jar and must verify it before the plugin
 * loader sees it: the marketplace installer and the default-plugin installer at startup.
 */
public final class PluginArtifacts {

  /**
   * An identifier that becomes part of a file name under the plugins temp directory, so it may
   * carry no separator or dot segment.
   */
  private static final Pattern SAFE_IDENTIFIER =
      Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,126}");

  private PluginArtifacts() {
    // utility
  }

  /**
   * Whether the value is safe to embed in a file name.
   *
   * @param value an id or version, may be null
   * @return true when it matches the permitted grammar
   */
  public static boolean isSafeIdentifier(String value) {
    return value != null && SAFE_IDENTIFIER.matcher(value).matches();
  }

  /**
   * Lower-case hex SHA-256 of a file, streamed.
   *
   * @param artifact the file
   * @return the digest
   * @throws IOException when the file cannot be read
   */
  public static String sha256(Path artifact) throws IOException {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
    try (InputStream in = Files.newInputStream(artifact)) {
      var buffer = new byte[8192];
      for (int read = in.read(buffer); read > 0; read = in.read(buffer)) {
        digest.update(buffer, 0, read);
      }
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  /**
   * A scratch file for an unverified download, in the given directory and readable by this process
   * alone where the filesystem supports POSIX permissions.
   *
   * @param directory the plugins staging directory, created if missing
   * @param prefix    file name prefix
   * @return the new empty file
   * @throws IOException when it cannot be created
   */
  public static Path createOwnerOnlyTempFile(Path directory, String prefix) throws IOException {
    Files.createDirectories(directory);
    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      var ownerOnly = PosixFilePermissions.asFileAttribute(
          PosixFilePermissions.fromString("rw-------"));
      return Files.createTempFile(directory, prefix, ".jar", ownerOnly);
    }
    return Files.createTempFile(directory, prefix, ".jar");
  }
}
