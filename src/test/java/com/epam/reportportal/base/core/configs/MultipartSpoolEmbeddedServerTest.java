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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.servlet.autoconfigure.MultipartAutoConfiguration;
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Uploads through a real embedded Tomcat: a part above the threshold is spooled to the configured
 * (initially missing) directory and removed when the request ends.
 */
@SpringBootTest(classes = MultipartSpoolEmbeddedServerTest.App.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.servlet.multipart.enabled=true",
        "spring.servlet.multipart.resolve-lazily=true",
        "spring.servlet.multipart.file-size-threshold=1MB",
        "spring.servlet.multipart.max-file-size=16MB",
        "spring.servlet.multipart.max-request-size=16MB",
        "spring.servlet.multipart.location=${rp.upload.temp.path}"
    })
class MultipartSpoolEmbeddedServerTest {

  private static final Path SPOOL = createBaseDir().resolve("not/yet/there/multipart");

  @Value("${local.server.port}")
  int port;

  @DynamicPropertySource
  static void spool(DynamicPropertyRegistry registry) {
    registry.add("rp.upload.temp.path", SPOOL::toString);
  }

  @Test
  void partAboveThresholdIsSpooledToConfiguredDirectoryAndRemovedAfterRequest() throws Exception {
    assertTrue(Files.isDirectory(SPOOL), "spool directory must be created at startup");

    var response = upload(new byte[3 * 1024 * 1024]);

    assertEquals(200, response.statusCode(), response.body());
    assertEquals("spooled=1 size=3145728", response.body());
    assertEquals(0, spoolFiles().length, "spooled part must be gone after the request");
  }

  @Test
  void partBelowThresholdStaysInMemory() throws Exception {
    var response = upload(new byte[100 * 1024]);

    assertEquals(200, response.statusCode(), response.body());
    assertEquals("spooled=0 size=102400", response.body());
    assertEquals(0, spoolFiles().length);
  }

  @Test
  void abortedUploadLeavesNothingBehind() throws Exception {
    String boundary = "----abort";
    byte[] head = ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; "
        + "filename=\"a.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n")
        .getBytes(StandardCharsets.UTF_8);
    int declared = head.length + 4 * 1024 * 1024;
    try (var socket = new java.net.Socket("localhost", port)) {
      var out = socket.getOutputStream();
      out.write(("POST /upload HTTP/1.1\r\nHost: localhost\r\nContent-Type: multipart/form-data; "
          + "boundary=" + boundary + "\r\nContent-Length: " + declared + "\r\n\r\n")
          .getBytes(StandardCharsets.UTF_8));
      out.write(head);
      out.write(new byte[2 * 1024 * 1024]);
      out.flush();
      Thread.sleep(300);
    }
    for (int i = 0; i < 50 && spoolFiles().length > 0; i++) {
      Thread.sleep(100);
    }
    assertEquals(0, spoolFiles().length, "aborted upload must not leave spooled parts");
  }

  private HttpResponse<String> upload(byte[] content) throws IOException, InterruptedException {
    String boundary = "----spool" + System.nanoTime();
    byte[] head = ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; "
        + "filename=\"a.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n")
        .getBytes(StandardCharsets.UTF_8);
    byte[] tail = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
    byte[] body = new byte[head.length + content.length + tail.length];
    System.arraycopy(head, 0, body, 0, head.length);
    System.arraycopy(content, 0, body, head.length, content.length);
    System.arraycopy(tail, 0, body, head.length + content.length, tail.length);
    var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/upload"))
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static String[] spoolFiles() throws IOException {
    try (Stream<Path> files = Files.list(SPOOL)) {
      return files.map(p -> p.getFileName().toString()).toArray(String[]::new);
    }
  }

  private static Path createBaseDir() {
    try {
      return Files.createTempDirectory("rp-spool-test");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @SpringBootConfiguration
  @ImportAutoConfiguration({TomcatServletWebServerAutoConfiguration.class,
      DispatcherServletAutoConfiguration.class, MultipartAutoConfiguration.class,
      WebMvcAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
  @Import({MultipartDataConfig.class, UploadController.class})
  static class App {

  }

  @RestController
  static class UploadController {

    @Value("${rp.upload.temp.path}")
    private String spool;

    @PostMapping("/upload")
    String upload(@RequestParam("file") MultipartFile file) throws IOException {
      String[] spooled;
      try (Stream<Path> files = Files.list(Path.of(spool))) {
        spooled = files.map(p -> p.getFileName().toString()).toArray(String[]::new);
      }
      assertTrue(Arrays.stream(spooled).allMatch(n -> n.startsWith("upload_") && n.endsWith(".tmp")),
          Arrays.toString(spooled));
      return "spooled=" + spooled.length + " size=" + file.getSize();
    }
  }
}
