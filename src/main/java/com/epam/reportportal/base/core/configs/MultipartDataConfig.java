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

import com.epam.reportportal.base.util.BinaryDataResponseWriter;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tuning for multipart file uploads in the service.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MultipartProperties.class)
public class MultipartDataConfig {

  /**
   * File name pattern of the parts Tomcat spools to disk ({@code upload_<uuid>_<n>.tmp}).
   */
  static final String SPOOL_FILE_GLOB = "upload_*.tmp";

  /**
   * Replaces the auto-configured {@link MultipartConfigElement} so that the spool directory
   * ({@code spring.servlet.multipart.location}, i.e. {@code rp.upload.temp.path}) exists and is
   * writable before the servlet container applies the multipart configuration. Tomcat refuses
   * uploads when the location does not exist. A directory that cannot be prepared fails the
   * startup instead of the first upload.
   *
   * @param properties bound {@code spring.servlet.multipart.*} properties
   * @return multipart configuration pointing at the prepared spool directory
   */
  @Bean
  public MultipartConfigElement multipartConfigElement(MultipartProperties properties) {
    Path spool = prepareSpoolDirectory(properties.getLocation());
    properties.setLocation(spool.toString());
    return properties.createMultipartConfig();
  }

  /**
   * Creates the spool directory if missing, checks that files can be written into it and removes
   * the leftovers of a previous run ({@code upload_*.tmp} regular files directly inside it; nothing
   * else is touched).
   *
   * @param location configured spool directory
   * @return absolute spool directory
   * @throws IllegalStateException if the location is not set or the directory cannot be created or
   *                               written
   */
  static Path prepareSpoolDirectory(String location) {
    if (location == null || location.isBlank()) {
      throw new IllegalStateException("Multipart upload spool directory is not configured: set "
          + "rp.upload.temp.path (RP_UPLOAD_TEMP_PATH) to a writable directory");
    }
    Path dir = Paths.get(location).toAbsolutePath().normalize();
    try {
      Files.createDirectories(dir);
      if (!Files.isDirectory(dir)) {
        throw new IOException("not a directory");
      }
      Files.delete(Files.createTempFile(dir, "probe_", ".tmp"));
    } catch (IOException | RuntimeException e) {
      throw new IllegalStateException("Multipart upload spool directory '" + dir
          + "' (rp.upload.temp.path) cannot be created or written to: " + e
          + ". Mount a writable volume there or set RP_UPLOAD_TEMP_PATH to a writable directory", e);
    }
    removeStaleParts(dir);
    return dir;
  }

  private static void removeStaleParts(Path dir) {
    int removed = 0;
    try (DirectoryStream<Path> stale = Files.newDirectoryStream(dir, SPOOL_FILE_GLOB)) {
      for (Path file : stale) {
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
          continue;
        }
        try {
          Files.delete(file);
          removed++;
        } catch (IOException e) {
          log.warn("Could not remove stale upload file {}: {}", file, e.toString());
        }
      }
    } catch (IOException e) {
      log.warn("Could not list upload spool directory {} for cleanup: {}", dir, e.toString());
    }
    if (removed > 0) {
      log.info("Removed {} stale upload file(s) from {}", removed, dir);
    }
  }

  @Bean
  public MimetypesFileTypeMap mimetypesFileTypeMap() {
    return new MimetypesFileTypeMap();
  }

  @Bean
  public AutoDetectParser autoDetectParser() {
    return new AutoDetectParser();
  }

  @Bean
  public BinaryDataResponseWriter binaryDataResponseWriter() {
    return new BinaryDataResponseWriter();
  }
}
