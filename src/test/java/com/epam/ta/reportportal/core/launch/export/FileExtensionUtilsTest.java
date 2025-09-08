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

package com.epam.ta.reportportal.core.launch.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link FileExtensionUtils}.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class FileExtensionUtilsTest {

  @ParameterizedTest
  @CsvSource({
      "file, text/plain, file.txt",
      "document, text/html, document.html",
      "style, text/css, style.css",
      "script, text/javascript, script.js",
      "data, text/xml, data.xml",
      "table, text/csv, table.csv",
      "code, text/x-php, code.php",
      "image, image/png, image.png",
      "photo, image/jpeg, photo.jpg",
      "icon, image/gif, icon.gif",
      "logo, image/svg+xml, logo.svg",
      "webp_image, image/webp, webp_image.webp",
      "report, application/pdf, report.pdf",
      "config, application/json, config.json",
      "archive, application/zip, archive.zip",
      "compressed, application/x-rar-compressed, compressed.rar",
      "tarfile, application/tar, tarfile.tar",
      "gzipfile, application/gzip, gzipfile.gz",
      "harfile, application/har+json, harfile.har"
  })
  void getFileNameWithExtension_ShouldAddExtension_WhenFileNameHasNoExtension(String fileName, 
                                                                             String contentType, 
                                                                             String expectedResult) {
    String result = FileExtensionUtils.getFileNameWithExtension(fileName, contentType);
    assertEquals(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource({
      "file.txt, text/plain, file.txt",
      "document.html, text/html, document.html",
      "style.css, text/css, style.css",
      "script.js, text/javascript, script.js",
      "data.xml, text/xml, data.xml",
      "table.csv, text/csv, table.csv",
      "code.php, text/x-php, code.php",
      "image.png, image/png, image.png",
      "photo.jpg, image/jpeg, photo.jpg",
      "icon.gif, image/gif, icon.gif",
      "logo.svg, image/svg+xml, logo.svg",
      "webp_image.webp, image/webp, webp_image.webp",
      "report.pdf, application/pdf, report.pdf",
      "config.json, application/json, config.json",
      "archive.zip, application/zip, archive.zip",
      "compressed.rar, application/x-rar-compressed, compressed.rar",
      "tarfile.tar, application/tar, tarfile.tar",
      "gzipfile.gz, application/gzip, gzipfile.gz",
      "harfile.har, application/har+json, harfile.har"
  })
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenFileNameAlreadyHasExtension(String fileName, 
                                                                                           String contentType, 
                                                                                           String expectedResult) {
    String result = FileExtensionUtils.getFileNameWithExtension(fileName, contentType);
    assertEquals(expectedResult, result);
  }

  @ParameterizedTest
  @CsvSource({
      "file, unknown/content-type, file",
      "document, application/unknown, document",
      "data, image/unknown, data"
  })
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenContentTypeIsNotSupported(String fileName, 
                                                                                          String contentType, 
                                                                                          String expectedResult) {
    String result = FileExtensionUtils.getFileNameWithExtension(fileName, contentType);
    assertEquals(expectedResult, result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnNull_WhenFileNameIsNull() {
    String result = FileExtensionUtils.getFileNameWithExtension(null, "text/plain");
    assertNull(result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnEmptyString_WhenFileNameIsEmpty() {
    String result = FileExtensionUtils.getFileNameWithExtension("", "text/plain");
    assertEquals("", result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnWhitespaceString_WhenFileNameIsWhitespace() {
    String result = FileExtensionUtils.getFileNameWithExtension("   ", "text/plain");
    assertEquals("   ", result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenContentTypeIsNull() {
    String result = FileExtensionUtils.getFileNameWithExtension("file", null);
    assertEquals("file", result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenContentTypeIsEmpty() {
    String result = FileExtensionUtils.getFileNameWithExtension("file", "");
    assertEquals("file", result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenContentTypeIsWhitespace() {
    String result = FileExtensionUtils.getFileNameWithExtension("file", "   ");
    assertEquals("file", result);
  }

  @ParameterizedTest
  @CsvSource({
      "file.name.txt, text/plain, file.name.txt",
      "document.test.html, text/html, document.test.html",
      "style.main.css, text/css, style.main.css",
      "script.min.js, text/javascript, script.min.js",
      "data.config.xml, text/xml, data.config.xml",
      "table.export.csv, text/csv, table.export.csv",
      "code.index.php, text/x-php, code.index.php",
      "image.icon.png, image/png, image.icon.png",
      "photo.thumb.jpg, image/jpeg, photo.thumb.jpg",
      "icon.small.gif, image/gif, icon.small.gif",
      "logo.brand.svg, image/svg+xml, logo.brand.svg",
      "webp_image.preview.webp, image/webp, webp_image.preview.webp",
      "report.final.pdf, application/pdf, report.final.pdf",
      "config.settings.json, application/json, config.settings.json",
      "archive.backup.zip, application/zip, archive.backup.zip",
      "compressed.data.rar, application/x-rar-compressed, compressed.data.rar",
      "tarfile.backup.tar, application/tar, tarfile.backup.tar",
      "gzipfile.compressed.gz, application/gzip, gzipfile.compressed.gz",
      "harfile.network.har, application/har+json, harfile.network.har"
  })
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenFileNameHasMultipleDots(String fileName, 
                                                                                        String contentType, 
                                                                                        String expectedResult) {
    String result = FileExtensionUtils.getFileNameWithExtension(fileName, contentType);
    assertEquals(expectedResult, result);
  }

  @Test
  void getFileNameWithExtension_ShouldHandleSpecialCharactersInFileName() {
    String result = FileExtensionUtils.getFileNameWithExtension("file-name_with.123", "text/plain");
    assertEquals("file-name_with.123", result);
  }

  @Test
  void getFileNameWithExtension_ShouldHandleUnicodeCharactersInFileName() {
    String result = FileExtensionUtils.getFileNameWithExtension("файл", "text/plain");
    assertEquals("файл.txt", result);
  }
}
