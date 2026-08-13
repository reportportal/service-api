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

package com.epam.reportportal.base.ws.controller;

/**
 * Resolved byte range for an attachment stream response.
 *
 * @param type        range result type
 * @param start       inclusive range start
 * @param end         inclusive range end
 * @param totalLength total attachment length
 */
public record AttachmentStreamRange(Type type, long start, long end, long totalLength) {

  /**
   * Creates a full-file read plan.
   */
  public static AttachmentStreamRange full(long totalLength) {
    return new AttachmentStreamRange(Type.FULL, 0, totalLength - 1, totalLength);
  }

  /**
   * Creates a partial-file read plan.
   */
  public static AttachmentStreamRange partial(long start, long end, long totalLength) {
    return new AttachmentStreamRange(Type.PARTIAL, start, end, totalLength);
  }

  /**
   * Creates an unsatisfiable range plan.
   */
  public static AttachmentStreamRange unsatisfiable(long totalLength) {
    return new AttachmentStreamRange(Type.UNSATISFIABLE, 0, -1, totalLength);
  }

  /**
   * Number of bytes to read from storage.
   */
  public long contentLength() {
    return type == Type.PARTIAL ? end - start + 1 : totalLength;
  }

  /**
   * Offset to use for the storage read.
   */
  public long offset() {
    return type == Type.PARTIAL ? start : 0;
  }

  /**
   * Range resolution result.
   */
  public enum Type {
    FULL, PARTIAL, UNSATISFIABLE
  }
}
