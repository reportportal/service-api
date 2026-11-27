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

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.annotation.Nullable;
import jakarta.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Takes ownership of uploaded parts whose content is read after the request that carried them has
 * ended (asynchronous attachment saving).
 *
 * <p>The servlet container deletes every part when its request ends. A part above
 * {@code spring.servlet.multipart.file-size-threshold} lives in a file in the spool directory
 * ({@code rp.upload.temp.path}), so a read after the response fails and the attachment is lost.
 * {@link #take(MultipartFile)} moves the content, on the request thread, into the owned-upload
 * directory: a subdirectory of the spool directory, so moving a spooled part is a rename on the same
 * file system; a part held in memory is written out. Nothing large is kept in heap.
 *
 * <p>The returned {@link OwnedUploadFile} must be closed by whoever consumes it, on success and on
 * failure. Files left by a JVM that died before closing them are removed at the next startup
 * ({@link com.epam.reportportal.base.core.configs.MultipartDataConfig}). The container never touches
 * this directory and the service never touches the container's {@code upload_*.tmp} files there at
 * runtime, so the two cleanups cannot interfere.
 */
@Slf4j
@Component
public class OwnedUploadStore {

  /**
   * Subdirectory of the spool directory holding owned uploads.
   */
  public static final String OWNED_DIRECTORY = "owned";

  /**
   * File name prefix of owned uploads.
   */
  public static final String FILE_PREFIX = "owned_";

  /**
   * File name suffix of owned uploads.
   */
  public static final String FILE_SUFFIX = ".part";

  /**
   * File name pattern of owned uploads, for cleanup.
   */
  public static final String FILE_GLOB = FILE_PREFIX + "*" + FILE_SUFFIX;

  private final Path directory;

  /**
   * @param multipartConfig the servlet multipart configuration; its location is the prepared spool
   *                        directory
   */
  @Autowired
  public OwnedUploadStore(MultipartConfigElement multipartConfig) {
    this(ownedDirectory(Paths.get(multipartConfig.getLocation())));
  }

  OwnedUploadStore(Path directory) {
    this.directory = directory.toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.directory);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Owned upload directory '" + this.directory + "' cannot be created: " + e, e);
    }
  }

  /**
   * @param spool the multipart spool directory
   * @return the owned-upload directory inside it
   */
  public static Path ownedDirectory(Path spool) {
    return spool.resolve(OWNED_DIRECTORY);
  }

  /**
   * @return the owned-upload directory
   */
  public Path getDirectory() {
    return directory;
  }

  /**
   * Moves the content of an uploaded part into a file the service owns. Must be called on the
   * request thread, before the request ends. Name, original file name, content type and size are
   * preserved.
   *
   * @param file the uploaded part, or {@code null}
   * @return the owned copy, or {@code null} if {@code file} is {@code null}. An {@link OwnedUploadFile}
   * passed in is returned as is.
   * @throws ReportPortalException ({@link ErrorType#BINARY_DATA_CANNOT_BE_SAVED}) if the content
   *                               cannot be moved; nothing is left behind then
   */
  @Nullable
  public OwnedUploadFile take(@Nullable MultipartFile file) {
    if (file == null) {
      return null;
    }
    if (file instanceof OwnedUploadFile owned) {
      return owned;
    }
    // Read the metadata before the move: a spooled part may report its size from the moved file.
    String name = file.getName();
    String originalFilename = file.getOriginalFilename();
    String contentType = file.getContentType();
    Path target = directory.resolve(FILE_PREFIX + UUID.randomUUID() + FILE_SUFFIX);
    try {
      // transferTo(File), not transferTo(Path): for a servlet part only the former goes through
      // Part.write, which renames a spooled part instead of copying it.
      file.transferTo(target.toFile());
      if (!Files.isRegularFile(target)) {
        throw new IOException("the content did not arrive in " + target);
      }
      return new OwnedUploadFile(name, originalFilename, contentType, Files.size(target), target);
    } catch (IOException | RuntimeException e) {
      deleteQuietly(target);
      log.error("Could not take over uploaded file '{}' into {}", originalFilename, directory, e);
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
          "Uploaded file '" + originalFilename + "' could not be stored for processing");
    }
  }

  private static void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException | RuntimeException e) {
      log.warn("Could not delete owned upload file {}: {}", path, e.toString());
    }
  }
}
