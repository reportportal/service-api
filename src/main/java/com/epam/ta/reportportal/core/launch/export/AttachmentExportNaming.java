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

/**
 * Builds attachment file names for launch export (report table and ZIP archive).
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public final class AttachmentExportNaming {

  private AttachmentExportNaming() {
  }

  /**
   * Returns {@code attachmentId + "_" + fileName}, optionally enriching the file name with an
   * extension derived from {@code contentType} when the name has no extension.
   *
   * @param attachmentId attachment primary key
   * @param fileName     stored file name
   * @param contentType  content type (used only when {@code inferExtension} is {@code true})
   */
  public static String prefixedFileName(long attachmentId, String fileName, String contentType) {
    return attachmentId + "_" + FileExtensionUtils.getFileNameWithExtension(fileName, contentType);
  }
}
