/*
 * Copyright 2026 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class BinaryDataResponseWriterTest {

  private final BinaryDataResponseWriter writer = new BinaryDataResponseWriter();

  @Test
  void writesTheContentAndClosesTheSourceStream() {
    byte[] content = "plugin file".getBytes(StandardCharsets.UTF_8);
    AtomicBoolean closed = new AtomicBoolean();
    InputStream source = new ByteArrayInputStream(content) {
      @Override
      public void close() throws IOException {
        closed.set(true);
        super.close();
      }
    };
    MockHttpServletResponse response = new MockHttpServletResponse();

    writer.write(new BinaryData("text/plain", (long) content.length, source), response);

    assertEquals("text/plain", response.getContentType());
    assertArrayEquals(content, response.getContentAsByteArray());
    assertTrue(closed.get());
  }

  @Test
  void closesTheSourceStreamWhenCopyingFails() {
    AtomicBoolean closed = new AtomicBoolean();
    InputStream source = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("broken");
      }

      @Override
      public void close() {
        closed.set(true);
      }
    };
    BinaryData binaryData = new BinaryData("text/plain", 1L, source);
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertThrows(ReportPortalException.class, () -> writer.write(binaryData, response));
    assertTrue(closed.get());
  }
}
