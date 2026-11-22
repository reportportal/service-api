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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Content of an uploaded part that the service owns: a file in the owned-upload directory that
 * outlives the HTTP request, unlike the request's own part, which the servlet container deletes when
 * the request ends. Keeps the part's name, original file name and content type, so code written
 * against {@link MultipartFile} works unchanged.
 *
 * <p>Whoever holds it must {@link #close()} it when the content is no longer needed; closing
 * deletes the file and is idempotent. Reading after closing fails.
 *
 * @see OwnedUploadStore#take(MultipartFile)
 */
@Slf4j
public final class OwnedUploadFile implements MultipartFile, Closeable {

  private final String name;
  private final String originalFilename;
  private final String contentType;
  private final long size;
  private final Path path;
  private final AtomicBoolean closed = new AtomicBoolean();

  OwnedUploadFile(String name, String originalFilename, String contentType, long size,
      Path path) {
    this.name = name;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
    this.size = size;
    this.path = path;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return originalFilename;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public byte[] getBytes() throws IOException {
    checkOpen();
    return Files.readAllBytes(path);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    checkOpen();
    return Files.newInputStream(path);
  }

  @Override
  public void transferTo(File dest) throws IOException {
    transferTo(dest.toPath());
  }

  /**
   * Copies the content; the owned file stays until {@link #close()}.
   */
  @Override
  public void transferTo(Path dest) throws IOException {
    checkOpen();
    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * @return the owned file (exposed for diagnostics and tests)
   */
  public Path getPath() {
    return path;
  }

  /**
   * Deletes the owned file. Never throws: a file that cannot be deleted is logged and left for the
   * startup cleanup.
   */
  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      try {
        Files.deleteIfExists(path);
      } catch (IOException | RuntimeException e) {
        log.warn("Could not delete owned upload file {}: {}", path, e.toString());
      }
    }
  }

  private void checkOpen() throws IOException {
    if (closed.get()) {
      throw new IOException("Owned upload file " + path + " has already been released");
    }
  }

  @Override
  public String toString() {
    return "OwnedUploadFile{name='" + name + "', originalFilename='" + originalFilename
        + "', size=" + size + ", path=" + path + '}';
  }
}
