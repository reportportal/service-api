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

import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Builds full and range-aware streaming responses for attachments.
 */
@Component
public class AttachmentStreamResponseFactory {

  /**
   * Resolves a single HTTP byte range against the attachment length.
   *
   * <p>Malformed and multi-range headers retain the existing behavior and fall back to a full
   * response.</p>
   *
   * @param request    HTTP request
   * @param fileLength total attachment length
   * @return resolved storage read range
   */
  public AttachmentStreamRange resolveRange(HttpServletRequest request, long fileLength) {
    var rangeHeader = request.getHeader(HttpHeaders.RANGE);
    if (rangeHeader == null || rangeHeader.isBlank()) {
      return AttachmentStreamRange.full(fileLength);
    }

    var range = parseRange(rangeHeader);
    if (range == null) {
      return AttachmentStreamRange.full(fileLength);
    }
    if (fileLength == 0) {
      return AttachmentStreamRange.unsatisfiable(fileLength);
    }

    long rangeStart = range.getRangeStart(fileLength);
    long rangeEnd = Math.min(range.getRangeEnd(fileLength), fileLength - 1);
    if (rangeStart >= fileLength || rangeStart > rangeEnd) {
      return AttachmentStreamRange.unsatisfiable(fileLength);
    }

    return AttachmentStreamRange.partial(rangeStart, rangeEnd, fileLength);
  }

  /**
   * Creates a response for an input stream that was already bounded to the resolved range.
   *
   * <p>The returned body owns the stream and closes it after writing; if the response cannot be
   * built, the stream is closed here.</p>
   *
   * @param binaryData attachment data
   * @param range      resolved storage range
   * @return streaming response
   */
  public ResponseEntity<StreamingResponseBody> toStreamingResponse(BinaryData binaryData,
      AttachmentStreamRange range) {
    try {
      return createRangeResponse(binaryData, range);
    } catch (RuntimeException e) {
      close(binaryData.getInputStream());
      throw e;
    }
  }

  /**
   * Creates a streaming HTTP response for the requested attachment range.
   *
   * <p>The returned body owns the stream and closes it after writing; if the response cannot be
   * built, the stream is closed here.</p>
   *
   * @param binaryData attachment data
   * @param request    HTTP request
   * @return streaming response
   */
  public ResponseEntity<StreamingResponseBody> toStreamingResponse(BinaryData binaryData,
      HttpServletRequest request) {
    try {
      return createResponse(binaryData, request);
    } catch (RuntimeException e) {
      close(binaryData.getInputStream());
      throw e;
    }
  }

  private ResponseEntity<StreamingResponseBody> createRangeResponse(BinaryData binaryData,
      AttachmentStreamRange range) {
    final var binaryStream = binaryData.getInputStream();
    if (range.type() == AttachmentStreamRange.Type.UNSATISFIABLE) {
      var headers = createHeaders(binaryData);
      close(binaryStream);
      headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + range.totalLength());
      return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(headers)
          .build();
    }
    if (binaryStream == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    var headers = createHeaders(binaryData);
    if (range.type() == AttachmentStreamRange.Type.PARTIAL) {
      headers.set(HttpHeaders.CONTENT_RANGE,
          "bytes " + range.start() + "-" + range.end() + "/" + range.totalLength());
      headers.setContentLength(range.contentLength());
    } else {
      headers.setContentLength(range.totalLength());
    }

    StreamingResponseBody responseBody = outputStream -> {
      try (InputStream inputStream = binaryStream) {
        IOUtils.copy(inputStream, outputStream);
      } catch (IOException e) {
        throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
      }
    };

    return ResponseEntity.status(
        range.type() == AttachmentStreamRange.Type.PARTIAL ? HttpStatus.PARTIAL_CONTENT
            : HttpStatus.OK).headers(headers).body(responseBody);
  }

  private ResponseEntity<StreamingResponseBody> createResponse(BinaryData binaryData,
      HttpServletRequest request) {
    final var binaryStream = binaryData.getInputStream();
    if (binaryStream == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    var headers = new HttpHeaders();

    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

    applyContentHeaders(headers, binaryData);

    var fileLength = binaryData.getLength();
    var requestHeader = request.getHeader(HttpHeaders.RANGE);

    HttpRange range = null;
    if (fileLength != null && requestHeader != null && !requestHeader.isBlank()) {
      range = parseRange(requestHeader);
    }

    if (range == null) {
      if (fileLength != null) {
        headers.setContentLength(fileLength);
      }

      StreamingResponseBody responseBody = outputStream -> {
        try (InputStream inputStream = binaryStream) {
          IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
          throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
        }
      };

      return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    long rangeStart = range.getRangeStart(fileLength);
    long rangeEnd = Math.min(range.getRangeEnd(fileLength), fileLength - 1);

    if (rangeStart >= fileLength || rangeStart > rangeEnd) {
      try {
        binaryStream.close();
      } catch (IOException ignored) {
      }

      headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength);
      return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(headers)
          .build();
    }

    headers.set(HttpHeaders.CONTENT_RANGE,
        "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
    headers.setContentLength(rangeEnd - rangeStart + 1);

    StreamingResponseBody responseBody = outputStream -> {
      try (InputStream inputStream = binaryStream) {
        StreamUtils.copyRange(inputStream, outputStream, rangeStart, rangeEnd);
      } catch (IOException e) {
        throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
      }
    };

    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers).body(responseBody);
  }

  private HttpRange parseRange(String rangeHeader) {
    try {
      var ranges = HttpRange.parseRanges(rangeHeader);
      return ranges.size() == 1 ? ranges.getFirst() : null;
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private HttpHeaders createHeaders(BinaryData binaryData) {
    var headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
    applyContentHeaders(headers, binaryData);
    return headers;
  }

  /**
   * The content type is client-supplied, so only types that cannot execute script in the browser
   * are rendered inline; everything else is forced to download.
   */
  private void applyContentHeaders(HttpHeaders headers, BinaryData binaryData) {
    var mediaType = Optional.ofNullable(binaryData.getContentType()).map(MediaType::parseMediaType);
    mediaType.ifPresent(headers::setContentType);

    var disposition = mediaType.filter(AttachmentStreamResponseFactory::isInlineSafe).isPresent()
        ? "inline" : "attachment";
    Optional.ofNullable(binaryData.getFileName())
        .map(n -> ContentDisposition.builder(disposition).filename(sanitizeFileName(n)).build())
        .ifPresentOrElse(headers::setContentDisposition, () -> {
          if ("attachment".equals(disposition)) {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, disposition);
          }
        });

    headers.set("X-Content-Type-Options", "nosniff");
    // A sandboxed document cannot load the browser's built-in PDF viewer, which would leave inline PDFs blank
    if (mediaType.filter(AttachmentStreamResponseFactory::isPdf).isEmpty()) {
      headers.set("Content-Security-Policy", "sandbox");
    }
  }

  private static boolean isPdf(MediaType type) {
    return "application".equals(type.getType()) && "pdf".equals(type.getSubtype());
  }

  private static boolean isInlineSafe(MediaType type) {
    var main = type.getType();
    var subtype = type.getSubtype();
    if ("image".equals(main)) {
      return !"svg+xml".equalsIgnoreCase(subtype) && !"svg".equalsIgnoreCase(subtype);
    }
    return "video".equals(main) || "audio".equals(main)
        || isPdf(type)
        || ("text".equals(main) && "plain".equals(subtype));
  }

  private void close(InputStream inputStream) {
    if (inputStream == null) {
      return;
    }
    try {
      inputStream.close();
    } catch (IOException ignored) {
    }
  }

  private String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[\n\r]+", " ");
  }
}
