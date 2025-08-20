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

/**
 * Simple unit tests for {@link FileExtensionUtils}.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class FileExtensionUtilsSimpleTest {

  @Test
  void getFileNameWithExtension_ShouldAddTxtExtension_WhenFileNameHasNoExtension() {
    String result = FileExtensionUtils.getFileNameWithExtension("file", "text/plain");
    assertEquals("file.txt", result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenFileNameAlreadyHasExtension() {
    String result = FileExtensionUtils.getFileNameWithExtension("file.txt", "text/plain");
    assertEquals("file.txt", result);
  }

  @Test
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenContentTypeIsNotSupported() {
    String result = FileExtensionUtils.getFileNameWithExtension("file", "unknown/content-type");
    assertEquals("file", result);
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
  void getFileNameWithExtension_ShouldReturnOriginalFileName_WhenContentTypeIsNull() {
    String result = FileExtensionUtils.getFileNameWithExtension("file", null);
    assertEquals("file", result);
  }

  @Test
  void getFileNameWithExtension_ShouldHandlePngExtension() {
    String result = FileExtensionUtils.getFileNameWithExtension("image", "image/png");
    assertEquals("image.png", result);
  }

  @Test
  void getFileNameWithExtension_ShouldHandlePdfExtension() {
    String result = FileExtensionUtils.getFileNameWithExtension("report", "application/pdf");
    assertEquals("report.pdf", result);
  }

  @Test
  void getFileNameWithExtension_ShouldHandleJsonExtension() {
    String result = FileExtensionUtils.getFileNameWithExtension("config", "application/json");
    assertEquals("config.json", result);
  }

  @Test
  void getFileNameWithExtension_ShouldHandleZipExtension() {
    String result = FileExtensionUtils.getFileNameWithExtension("archive", "application/zip");
    assertEquals("archive.zip", result);
  }
}
