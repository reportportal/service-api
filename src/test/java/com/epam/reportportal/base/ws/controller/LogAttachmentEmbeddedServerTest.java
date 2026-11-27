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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.file.upload.OwnedUploadStore;
import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.servlet.MultipartConfigElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@code POST /v1/{projectKey}/log} (multipart, synchronous reporting API) through a real embedded
 * Tomcat that serves this test's application context, so that parts above
 * {@code spring.servlet.multipart.file-size-threshold} are spooled to disk and deleted by Tomcat
 * when the request ends, as in production.
 *
 * <p>The handler answers before it saves the log and its attachment. To make "the save happens
 * after the request has ended" deterministic, the test holds a row lock on the launch: the deferred
 * log insert waits on it (foreign key), the test sees the response, waits until Tomcat has cleaned
 * the request's parts, and only then releases the lock. Without owned upload files the attachment of
 * a spooled part is then lost ({@code NoSuchFileException}) while the client got 201.
 *
 * <p>Data is committed (the server threads do not see the test transaction) and removed afterwards.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LogAttachmentEmbeddedServerTest extends BaseMvcTest {

  private static final long PROJECT_ID = 2L;
  private static final long USER_ID = 2L;
  private static final Duration SAVE_TIMEOUT = Duration.ofSeconds(20);

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private MultipartConfigElement multipartConfig;

  @Autowired
  private OwnedUploadStore ownedUploadStore;

  @Autowired
  @Qualifier("attachmentDataStoreService")
  private DataStoreService attachmentDataStore;

  private JdbcTemplate jdbc;
  private WebServer server;
  private long launchId;
  private String launchUuid;

  @BeforeEach
  void startServerAndCreateLaunch() {
    jdbc = new JdbcTemplate(dataSource);
    launchUuid = UUID.randomUUID().toString();
    launchId = jdbc.queryForObject("insert into launch (uuid, project_id, user_id, name, "
            + "description, start_time, number, last_modified, mode, status) values (?, ?, ?, ?, "
            + "'desc', now(), 1, now(), 'DEFAULT', 'IN_PROGRESS') returning id", Long.class,
        launchUuid, PROJECT_ID, USER_ID, "attachment-lifetime-" + launchUuid);

    var factory = new TomcatServletWebServerFactory(0);
    // the application's own servlets and filters (dispatcher with its multipart configuration,
    // security filter chain, ...), as the production server registers them
    List<ServletContextInitializer> initializers = new ServletContextInitializerBeans(context)
        .stream().toList();
    server = factory.getWebServer(initializers.toArray(ServletContextInitializer[]::new));
    server.start();
  }

  @AfterEach
  void stopServerAndRemoveData() {
    if (server != null) {
      server.stop();
    }
    for (String fileId : jdbc.queryForList("select file_id from attachment where launch_id = ?",
        String.class, launchId)) {
      attachmentDataStore.delete(fileId);
    }
    jdbc.update("update log set attachment_id = null where launch_id = ?", launchId);
    jdbc.update("delete from attachment where launch_id = ?", launchId);
    jdbc.update("delete from log where launch_id = ?", launchId);
    jdbc.update("delete from launch where id = ?", launchId);
  }

  /**
   * Reproduces the original loss: before owned upload files this failed with "attachment lost" (the
   * log was saved without it; the server logged {@code NoSuchFileException .../upload_...tmp}).
   */
  @Test
  void attachmentAboveThresholdIsStoredAfterTheRequestHasEnded() throws Exception {
    byte[] content = randomBytes(5 * 1024 * 1024);
    assertTrue(content.length > multipartConfig.getFileSizeThreshold(),
        "the part must be spooled to disk");

    String logUuid = reportLogWithAttachmentWhileLogSaveIsHeld(content, "big.bin", false);

    Map<String, Object> attachment = awaitAttachment(logUuid);
    assertEquals((long) content.length, ((Number) attachment.get("file_size")).longValue());
    assertEquals("big.bin", attachment.get("file_name"));
    assertArrayEquals(content, load((String) attachment.get("file_id")),
        "stored content must be the uploaded content");
    assertHeldInOneOwnedFileWhilePendingAndReleased();
  }

  @Test
  void smallAttachmentHeldInMemoryIsStored() throws Exception {
    byte[] content = randomBytes(512 * 1024);
    assertTrue(content.length < multipartConfig.getFileSizeThreshold());

    String logUuid = reportLogWithAttachmentWhileLogSaveIsHeld(content, "small.bin", false);

    Map<String, Object> attachment = awaitAttachment(logUuid);
    assertEquals((long) content.length, ((Number) attachment.get("file_size")).longValue());
    assertArrayEquals(content, load((String) attachment.get("file_id")));
    assertHeldInOneOwnedFileWhilePendingAndReleased();
  }

  @Test
  void failedSaveAfterTheResponseReleasesTheOwnedFile() throws Exception {
    byte[] content = randomBytes(3 * 1024 * 1024);

    // the launch is deleted while the log insert waits: the deferred save fails on the foreign key
    String logUuid = reportLogWithAttachmentWhileLogSaveIsHeld(content, "doomed.bin", true);

    assertHeldInOneOwnedFileWhilePendingAndReleased();
    assertEquals(0, jdbc.queryForObject("select count(*) from log where uuid = ?", Integer.class,
        logUuid));
  }

  private void assertHeldInOneOwnedFileWhilePendingAndReleased() throws Exception {
    assertEquals(1, ownedWhilePending,
        "while the save is pending the content is held in exactly one owned file");
    awaitNoNewOwnedFiles();
  }

  private Set<Path> ownedBefore;
  private int ownedWhilePending;

  private String reportLogWithAttachmentWhileLogSaveIsHeld(byte[] content, String fileName,
      boolean deleteLaunchInstead) throws Exception {
    Path spool = Path.of(multipartConfig.getLocation());
    Set<Path> spooledBefore = list(spool, "upload_*.tmp");
    ownedBefore = list(ownedUploadStore.getDirectory(), OwnedUploadStore.FILE_GLOB);
    String logUuid = UUID.randomUUID().toString();

    try (Connection lock = dataSource.getConnection()) {
      lock.setAutoCommit(false);
      try (PreparedStatement st = lock.prepareStatement(
          "select id from launch where id = ? for update")) {
        st.setLong(1, launchId);
        st.executeQuery().close();
      }
      try {
        HttpResponse<String> response = postLog(logUuid, fileName, content);
        assertEquals(201, response.statusCode(), response.body());
        assertTrue(response.body().contains(logUuid), response.body());

        // Tomcat removes the request's parts once the request is recycled
        await("Tomcat to clean the request's parts",
            () -> difference(list(spool, "upload_*.tmp"), spooledBefore).isEmpty());
        ownedWhilePending = difference(
            list(ownedUploadStore.getDirectory(), OwnedUploadStore.FILE_GLOB), ownedBefore).size();

        if (deleteLaunchInstead) {
          try (PreparedStatement st = lock.prepareStatement("delete from launch where id = ?")) {
            st.setLong(1, launchId);
            st.executeUpdate();
          }
          lock.commit();
        }
      } finally {
        if (!lock.getAutoCommit()) {
          lock.rollback();
        }
      }
    }
    return logUuid;
  }

  private HttpResponse<String> postLog(String logUuid, String fileName, byte[] content)
      throws IOException, InterruptedException {
    String json = "[{\"launchUuid\":\"" + launchUuid + "\",\"uuid\":\"" + logUuid
        + "\",\"time\":\"2025-01-01T00:00:00Z\",\"message\":\"with attachment\",\"level\":\"ERROR\","
        + "\"file\":{\"name\":\"" + fileName + "\"}}]";
    String boundary = "----rp" + System.nanoTime();
    var body = new ByteArrayOutputStream();
    body.writeBytes(("--" + boundary + "\r\nContent-Disposition: form-data; "
        + "name=\"json_request_part\"\r\nContent-Type: application/json\r\n\r\n" + json + "\r\n")
        .getBytes(StandardCharsets.UTF_8));
    body.writeBytes(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; "
        + "filename=\"" + fileName + "\"\r\nContent-Type: application/octet-stream\r\n\r\n")
        .getBytes(StandardCharsets.UTF_8));
    body.writeBytes(content);
    body.writeBytes(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
    var request = HttpRequest.newBuilder(
            URI.create("http://localhost:" + server.getPort() + DEFAULT_PROJECT_BASE_URL + "/log"))
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .header("Authorization", "Bearer " + oAuthHelper.getDefaultToken())
        .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build();
    try (var client = HttpClient.newHttpClient()) {
      return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
  }

  private Map<String, Object> awaitAttachment(String logUuid) throws InterruptedException {
    String sql = "select a.file_id, a.file_size, a.file_name from log l "
        + "join attachment a on a.id = l.attachment_id where l.uuid = ?";
    long deadline = System.nanoTime() + SAVE_TIMEOUT.toNanos();
    while (System.nanoTime() < deadline) {
      List<Map<String, Object>> rows = jdbc.queryForList(sql, logUuid);
      if (!rows.isEmpty()) {
        return rows.getFirst();
      }
      Thread.sleep(100);
    }
    Integer logs = jdbc.queryForObject("select count(*) from log where uuid = ?", Integer.class,
        logUuid);
    throw new AssertionError("attachment lost: the client got 201, the log was "
        + (logs > 0 ? "saved" : "not saved") + ", but no attachment was stored within "
        + SAVE_TIMEOUT);
  }

  private byte[] load(String fileId) throws IOException {
    try (InputStream in = attachmentDataStore.load(fileId).orElse(null)) {
      assertNotNull(in, "no stored content for " + fileId);
      return in.readAllBytes();
    }
  }

  private void awaitNoNewOwnedFiles() throws Exception {
    await("owned upload files to be released", () -> difference(
        list(ownedUploadStore.getDirectory(), OwnedUploadStore.FILE_GLOB), ownedBefore).isEmpty());
  }

  private static void await(String what, ThrowingSupplier condition) throws Exception {
    long deadline = System.nanoTime() + SAVE_TIMEOUT.toNanos();
    while (!condition.get()) {
      if (System.nanoTime() > deadline) {
        throw new AssertionError("timed out waiting for " + what);
      }
      Thread.sleep(50);
    }
  }

  @FunctionalInterface
  private interface ThrowingSupplier {

    boolean get() throws Exception;
  }

  private static Set<Path> list(Path dir, String glob) throws IOException {
    Set<Path> result = new HashSet<>();
    try (var stream = Files.newDirectoryStream(dir, glob)) {
      stream.forEach(result::add);
    }
    return result;
  }

  private static Set<Path> difference(Set<Path> now, Set<Path> before) {
    return now.stream().filter(p -> !before.contains(p)).collect(Collectors.toSet());
  }

  private static byte[] randomBytes(int size) {
    byte[] bytes = new byte[size];
    new Random(size).nextBytes(bytes);
    return bytes;
  }
}
