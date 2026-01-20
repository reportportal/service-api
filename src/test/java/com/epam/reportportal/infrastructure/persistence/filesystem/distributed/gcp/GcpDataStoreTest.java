/*
 * Copyright 2023 EPAM Systems
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

package com.epam.reportportal.infrastructure.persistence.filesystem.distributed.gcp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import com.google.cloud.NoCredentials;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class GcpDataStoreTest {

  private static final String FILE_NAME = "someFile";
  private static final String BUCKET_PREFIX = "prj-";
  private static final String BUCKET_POSTFIX = "-postfix";
  private static final String DEFAULT_BUCKET_NAME = "rp-bucket";

  private static final int PORT = findFreePort();

  @Container
  private static final GenericContainer<?> gcsContainer = new GenericContainer<>(
      DockerImageName.parse("fsouza/fake-gcs-server:latest"))
      .withCommand("-scheme", "http", "-external-url", "http://localhost:" + PORT);

  static {
    gcsContainer.setPortBindings(Collections.singletonList(PORT + ":4443"));
  }

  private Storage storage;
  private GcpDataStore gcpDataStore;

  private static int findFreePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setUp() {
    String endpoint = "http://localhost:" + PORT;

    storage = StorageOptions.newBuilder()
        .setHost(endpoint)
        .setProjectId("test-project")
        .setCredentials(NoCredentials.getInstance())
        .build()
        .getService();

    // Create bucket if it doesn't exist
    if (storage.get(DEFAULT_BUCKET_NAME) == null) {
      storage.create(BucketInfo.of(DEFAULT_BUCKET_NAME));
    }

    FeatureFlagHandler featureFlagHandler = mock(FeatureFlagHandler.class);
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(true);

    gcpDataStore = new GcpDataStore(storage, BUCKET_PREFIX, BUCKET_POSTFIX, DEFAULT_BUCKET_NAME,
        featureFlagHandler);
  }

  @Test
  void save() {
    String filePath = FILE_NAME;
    byte[] testData = "test content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);

    String result = gcpDataStore.save(filePath, inputStream);

    assertEquals(filePath, result);
    assertTrue(gcpDataStore.exists(filePath));
  }

  @Test
  void load() throws IOException {
    String filePath = FILE_NAME;
    byte[] testData = "test content for load".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);

    gcpDataStore.save(filePath, inputStream);

    InputStream loaded = gcpDataStore.load(filePath);

    assertNotNull(loaded);
    byte[] loadedData = loaded.readAllBytes();
    assertArrayEquals(testData, loadedData);
  }

  @Test
  void delete() {
    String filePath = FILE_NAME;
    byte[] testData = "test content for delete".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);

    gcpDataStore.save(filePath, inputStream);
    assertTrue(gcpDataStore.exists(filePath));

    gcpDataStore.delete(filePath);

    assertFalse(gcpDataStore.exists(filePath));
  }
}
