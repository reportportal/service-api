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

package com.epam.reportportal.base.core.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;

class MultipartDataConfigTest {

  @TempDir
  Path tmp;

  @Test
  void createsMissingDirectoryWithParents() throws IOException {
    Path dir = tmp.resolve("a/b/multipart");

    Path prepared = MultipartDataConfig.prepareSpoolDirectory(dir.toString());

    assertEquals(dir.toAbsolutePath().normalize(), prepared);
    assertTrue(Files.isDirectory(dir));
    assertEquals(Set.of("owned"), names(dir), "only the owned-upload subdirectory is created");
    assertTrue(Files.isDirectory(dir.resolve("owned")));
  }

  @Test
  void multipartConfigPointsAtPreparedDirectory() {
    Path dir = tmp.resolve("spool");
    MultipartProperties properties = new MultipartProperties();
    properties.setLocation(dir.toString());

    var config = new MultipartDataConfig().multipartConfigElement(properties);

    assertTrue(Files.isDirectory(dir));
    assertEquals(dir.toString(), config.getLocation());
  }

  @Test
  void failsWhenLocationIsNotSet() {
    var e = assertThrows(IllegalStateException.class,
        () -> MultipartDataConfig.prepareSpoolDirectory(" "));
    assertTrue(e.getMessage().contains("rp.upload.temp.path"), e.getMessage());
  }

  @Test
  void failsWhenDirectoryCannotBeCreated() throws IOException {
    Path blocker = Files.createFile(tmp.resolve("not-a-dir"));

    var e = assertThrows(IllegalStateException.class,
        () -> MultipartDataConfig.prepareSpoolDirectory(blocker.resolve("multipart").toString()));

    assertTrue(e.getMessage().contains("cannot be created or written to"), e.getMessage());
    assertTrue(e.getMessage().contains("rp.upload.temp.path"), e.getMessage());
  }

  @Test
  void failsWhenLocationIsAFile() throws IOException {
    Path file = Files.createFile(tmp.resolve("file"));

    var e = assertThrows(IllegalStateException.class,
        () -> MultipartDataConfig.prepareSpoolDirectory(file.toString()));

    assertTrue(e.getMessage().contains("cannot be created or written to"), e.getMessage());
  }

  @Test
  void failsWhenDirectoryIsNotWritable() throws IOException {
    Path dir = Files.createDirectory(tmp.resolve("readonly"));
    org.junit.jupiter.api.Assumptions.assumeTrue(
        dir.toFile().setWritable(false) && !Files.isWritable(dir),
        "cannot make a directory read-only here (running as root?)");
    try {
      var e = assertThrows(IllegalStateException.class,
          () -> MultipartDataConfig.prepareSpoolDirectory(dir.toString()));
      assertTrue(e.getMessage().contains("cannot be created or written to"), e.getMessage());
    } finally {
      dir.toFile().setWritable(true);
    }
  }

  @Test
  void removesOnlyStaleUploadPartsFromTheSpoolDirectory() throws IOException {
    Path dir = Files.createDirectory(tmp.resolve("spool"));
    Path stale = Files.writeString(dir.resolve("upload_1234_5.tmp"), "x");
    Path other = Files.writeString(dir.resolve("keep.jar"), "x");
    Path nested = Files.createDirectory(dir.resolve("sub"));
    Path nestedFile = Files.writeString(nested.resolve("upload_1_1.tmp"), "x");
    Path dirNamedLikePart = Files.createDirectory(dir.resolve("upload_dir.tmp"));

    MultipartDataConfig.prepareSpoolDirectory(dir.toString());

    assertFalse(Files.exists(stale));
    assertTrue(Files.exists(other));
    assertTrue(Files.exists(nestedFile));
    assertTrue(Files.isDirectory(dirNamedLikePart));
    assertEquals(Set.of("keep.jar", "sub", "upload_dir.tmp", "owned"), names(dir));
  }

  @Test
  void removesOnlyStaleOwnedUploadsFromTheOwnedSubdirectory() throws IOException {
    Path dir = Files.createDirectory(tmp.resolve("spool"));
    Path owned = Files.createDirectory(dir.resolve("owned"));
    Path stale = Files.writeString(owned.resolve("owned_0b1d.part"), "x");
    Path tomcatNamed = Files.writeString(owned.resolve("upload_1_1.tmp"), "x");
    Path other = Files.writeString(owned.resolve("keep.txt"), "x");
    Path dirNamedLikeOwned = Files.createDirectory(owned.resolve("owned_dir.part"));

    MultipartDataConfig.prepareSpoolDirectory(dir.toString());

    assertFalse(Files.exists(stale));
    assertTrue(Files.exists(tomcatNamed), "the spool glob applies to the spool directory only");
    assertTrue(Files.exists(other));
    assertTrue(Files.isDirectory(dirNamedLikeOwned));
    assertEquals(Set.of("upload_1_1.tmp", "keep.txt", "owned_dir.part"), names(owned));
  }

  @Test
  void failsWhenOwnedSubdirectoryCannotBeCreated() throws IOException {
    Path dir = Files.createDirectory(tmp.resolve("spool"));
    Files.writeString(dir.resolve("owned"), "a file in the way");

    var e = assertThrows(IllegalStateException.class,
        () -> MultipartDataConfig.prepareSpoolDirectory(dir.toString()));

    assertTrue(e.getMessage().contains("Owned upload directory"), e.getMessage());
  }

  private static Set<String> names(Path dir) throws IOException {
    try (var s = Files.list(dir)) {
      return s.map(p -> p.getFileName().toString()).collect(java.util.stream.Collectors.toSet());
    }
  }
}
