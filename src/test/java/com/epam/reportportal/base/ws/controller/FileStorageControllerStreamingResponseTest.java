package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@ExtendWith(MockitoExtension.class)
class FileStorageControllerStreamingResponseTest {

  @Mock
  private HttpServletRequest request;

  private AttachmentStreamResponseFactory responseFactory;

  @BeforeEach
  void setUp() {
    responseFactory = new AttachmentStreamResponseFactory();
  }

  @Test
  void returnsNoContentWhenStreamIsMissing() {
    var binaryData = mock(BinaryData.class);
    when(binaryData.getInputStream()).thenReturn(null);

    var response = invokeToStreamingResponse(binaryData);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void returnsFullContentWhenRangeHeaderAbsent() throws IOException {
    var data = "test-data".getBytes(StandardCharsets.UTF_8);
    var binaryData = mock(BinaryData.class);
    when(binaryData.getInputStream()).thenReturn(new ByteArrayInputStream(data));
    when(binaryData.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(binaryData.getFileName()).thenReturn("test.txt");
    when(binaryData.getLength()).thenReturn((long) data.length);
    when(request.getHeader(HttpHeaders.RANGE)).thenReturn(null);

    var response = invokeToStreamingResponse(binaryData);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
    assertEquals("inline", response.getHeaders().getContentDisposition().getType());
    assertEquals("test.txt", response.getHeaders().getContentDisposition().getFilename());
    assertEquals(data.length, response.getHeaders().getContentLength());
    assertEquals("bytes", response.getHeaders().getFirst(HttpHeaders.ACCEPT_RANGES));

    var body = response.getBody();
    assertNotNull(body);
    var output = new ByteArrayOutputStream();
    body.writeTo(output);

    assertArrayEquals(data, output.toByteArray());
  }

  @Test
  void returnsPartialContentWhenRangeHeaderPresent() throws IOException {
    var data = "HelloWorld".getBytes(StandardCharsets.UTF_8);
    var binaryData = mock(BinaryData.class);
    when(binaryData.getInputStream()).thenReturn(new ByteArrayInputStream(data));
    when(binaryData.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(binaryData.getFileName()).thenReturn("greeting.txt");
    when(binaryData.getLength()).thenReturn((long) data.length);
    when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=2-5");

    var response = invokeToStreamingResponse(binaryData);

    assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
    assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
    assertEquals("inline", response.getHeaders().getContentDisposition().getType());
    assertEquals("greeting.txt", response.getHeaders().getContentDisposition().getFilename());
    assertEquals("bytes 2-5/10", response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
    assertEquals(4, response.getHeaders().getContentLength());

    var body = response.getBody();
    assertNotNull(body);
    var output = new ByteArrayOutputStream();
    body.writeTo(output);

    var expected = "lloW".getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, output.toByteArray());
  }

  @Test
  void returnsRequestedRangeNotSatisfiableWhenRangeExceedsLength() {
    var data = new byte[100];
    var binaryData = mock(BinaryData.class);
    when(binaryData.getInputStream()).thenReturn(new ByteArrayInputStream(data));
    when(binaryData.getContentType()).thenReturn(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    when(binaryData.getFileName()).thenReturn("data.bin");
    when(binaryData.getLength()).thenReturn((long) data.length);
    when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=1000-");

    var response = invokeToStreamingResponse(binaryData);

    assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    assertEquals("inline", response.getHeaders().getContentDisposition().getType());
    assertEquals("data.bin", response.getHeaders().getContentDisposition().getFilename());
    assertEquals("bytes", response.getHeaders().getFirst(HttpHeaders.ACCEPT_RANGES));
    assertEquals("bytes */100", response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
    assertNull(response.getBody());
  }

  @Test
  void returnsAlreadyBoundedPartialStreamWithoutSkippingAgain() throws IOException {
    var data = "lloW".getBytes(StandardCharsets.UTF_8);
    var binaryData = mock(BinaryData.class);
    when(binaryData.getInputStream()).thenReturn(new ByteArrayInputStream(data));
    when(binaryData.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);
    when(binaryData.getFileName()).thenReturn("greeting.txt");
    when(request.getHeader(HttpHeaders.RANGE)).thenReturn("bytes=2-5");

    var range = responseFactory.resolveRange(request, 10);
    var response = responseFactory.toStreamingResponse(binaryData, range);

    assertEquals(AttachmentStreamRange.Type.PARTIAL, range.type());
    assertEquals(2, range.offset());
    assertEquals(4, range.contentLength());
    assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
    assertEquals("bytes 2-5/10", response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));

    var body = response.getBody();
    assertNotNull(body);
    var output = new ByteArrayOutputStream();
    body.writeTo(output);
    assertArrayEquals(data, output.toByteArray());
  }

  private ResponseEntity<StreamingResponseBody> invokeToStreamingResponse(BinaryData binaryData) {
    return responseFactory.toStreamingResponse(binaryData, request);
  }
}
