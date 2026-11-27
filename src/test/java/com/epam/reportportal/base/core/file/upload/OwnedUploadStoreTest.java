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

package com.epam.reportportal.base.core.file.upload;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class OwnedUploadStoreTest {

  @TempDir
  Path spool;

  @Test
  void ownedDirectoryIsASubdirectoryOfTheSpoolAndIsCreated() {
    var store = new OwnedUploadStore(new MultipartConfigElement(spool.toString()));

    assertEquals(spool.resolve("owned"), store.getDirectory());
    assertTrue(Files.isDirectory(store.getDirectory()));
  }

  @Test
  void takesContentAndMetadataAndOutlivesTheOriginal() throws IOException {
    var store = new OwnedUploadStore(OwnedUploadStore.ownedDirectory(spool));
    byte[] content = "hello attachment".getBytes(StandardCharsets.UTF_8);
    var original = new MockMultipartFile("file", "report.txt", "text/plain", content);

    OwnedUploadFile owned = store.take(original);

    assertEquals("file", owned.getName());
    assertEquals("report.txt", owned.getOriginalFilename());
    assertEquals("text/plain", owned.getContentType());
    assertEquals(content.length, owned.getSize());
    assertFalse(owned.isEmpty());
    assertEquals(store.getDirectory(), owned.getPath().getParent());
    assertTrue(owned.getPath().getFileName().toString().matches("owned_.+\\.part"),
        owned.getPath().toString());
    assertArrayEquals(content, owned.getBytes());
    try (InputStream in = owned.getInputStream()) {
      assertArrayEquals(content, in.readAllBytes());
    }
    Path copy = spool.resolve("copy.txt");
    owned.transferTo(copy.toFile());
    assertArrayEquals(content, Files.readAllBytes(copy));
    assertTrue(Files.exists(owned.getPath()), "transferTo copies, the owned file stays");

    owned.close();

    assertFalse(Files.exists(owned.getPath()));
    assertEquals(List.of(), ownedFiles(store));
  }

  @Test
  void usesTransferToFileSoThatASpooledPartIsRenamedNotCopied() throws IOException {
    var store = new OwnedUploadStore(OwnedUploadStore.ownedDirectory(spool));
    Path spooled = Files.writeString(spool.resolve("upload_1_1.tmp"), "spooled");
    MultipartFile part = mock(MultipartFile.class);
    when(part.getName()).thenReturn("file");
    when(part.getOriginalFilename()).thenReturn("big.bin");
    when(part.getContentType()).thenReturn("application/octet-stream");
    doAnswer(inv -> Files.move(spooled, inv.<File>getArgument(0).toPath()))
        .when(part).transferTo(any(File.class));

    try (OwnedUploadFile owned = store.take(part)) {
      assertFalse(Files.exists(spooled));
      assertEquals("spooled", new String(owned.getBytes(), StandardCharsets.UTF_8));
      assertEquals(7, owned.getSize());
    }
    assertEquals(List.of(), ownedFiles(store));
  }

  @Test
  void emptyFileIsTakenAsEmpty() throws IOException {
    var store = new OwnedUploadStore(OwnedUploadStore.ownedDirectory(spool));

    try (OwnedUploadFile owned = store.take(new MockMultipartFile("f", "e.txt", null, new byte[0]))) {
      assertTrue(owned.isEmpty());
      assertEquals(0, owned.getSize());
      assertNull(owned.getContentType());
    }
  }

  @Test
  void nullStaysNullAndOwnedFileIsReturnedAsIs() {
    var store = new OwnedUploadStore(OwnedUploadStore.ownedDirectory(spool));
    assertNull(store.take(null));

    OwnedUploadFile owned = store.take(new MockMultipartFile("f", "a", null, new byte[]{1}));
    assertSame(owned, store.take(owned));
    owned.close();
  }

  @Test
  void failedTransferLeavesNothingAndFailsLoudly() throws IOException {
    var store = new OwnedUploadStore(OwnedUploadStore.ownedDirectory(spool));
    MultipartFile part = mock(MultipartFile.class);
    when(part.getOriginalFilename()).thenReturn("broken.bin");
    doAnswer(inv -> {
      Files.writeString(inv.<File>getArgument(0).toPath(), "half");
      throw new IOException("disk full");
    }).when(part).transferTo(any(File.class));

    var e = assertThrows(ReportPortalException.class, () -> store.take(part));

    assertEquals(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, e.getErrorType());
    assertTrue(e.getMessage().contains("broken.bin"), e.getMessage());
    assertEquals(List.of(), ownedFiles(store));
  }

  @Test
  void closeIsIdempotentAndReadsAfterCloseFail() {
    var store = new OwnedUploadStore(OwnedUploadStore.ownedDirectory(spool));
    OwnedUploadFile owned = store.take(new MockMultipartFile("f", "a", null, new byte[]{1, 2}));

    owned.close();
    owned.close();

    assertThrows(IOException.class, owned::getInputStream);
    assertThrows(IOException.class, owned::getBytes);
    assertThrows(IOException.class, () -> owned.transferTo(spool.resolve("x")));
    assertEquals(2, owned.getSize(), "metadata stays available for logging");
  }

  private static List<Path> ownedFiles(OwnedUploadStore store) throws IOException {
    try (Stream<Path> files = Files.list(store.getDirectory())) {
      return files.toList();
    }
  }
}
