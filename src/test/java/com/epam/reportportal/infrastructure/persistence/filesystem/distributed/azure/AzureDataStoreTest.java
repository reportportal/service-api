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

package com.epam.reportportal.infrastructure.persistence.filesystem.distributed.azure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.filesystem.distributed.s3.S3DataStore;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.opendal.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class AzureDataStoreTest {

  private static final String FILE_NAME = "someFile";
  private static final String BUCKET_PREFIX = "prj-";
  private static final String BUCKET_POSTFIX = "-postfix";
  private static final String DEFAULT_BUCKET_NAME = "rp-bucket";
  
  // Azurite default credentials
  private static final String AZURE_ACCOUNT_NAME = "devstoreaccount1";
  private static final String AZURE_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

  @Container
  private static final GenericContainer<?> azuriteContainer = new GenericContainer<>(
      DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:latest"))
      .withExposedPorts(10000);

  private Operator operator;
  private S3DataStore s3DataStore;

  @BeforeEach
  void setUp() {
    String host = azuriteContainer.getHost();
    Integer port = azuriteContainer.getMappedPort(10000);
    String endpoint = String.format("http://%s:%d/%s", host, port, AZURE_ACCOUNT_NAME);
    
    String connectionString = String.format(
        "DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s;BlobEndpoint=%s;",
        AZURE_ACCOUNT_NAME, AZURE_ACCOUNT_KEY, endpoint
    );

    // Create container (bucket) using Azure SDK
    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
        .connectionString(connectionString)
        .buildClient();
    
    if (!blobServiceClient.getBlobContainerClient(DEFAULT_BUCKET_NAME).exists()) {
        blobServiceClient.createBlobContainer(DEFAULT_BUCKET_NAME);
    }

    // Configure OpenDAL operator for Azure Blob Storage
    Map<String, String> config = new HashMap<>();
    config.put("account_name", AZURE_ACCOUNT_NAME);
    config.put("account_key", AZURE_ACCOUNT_KEY);
    config.put("endpoint", endpoint);
    config.put("container", DEFAULT_BUCKET_NAME);

    operator = Operator.of("azblob", config);

    FeatureFlagHandler featureFlagHandler = mock(FeatureFlagHandler.class);
    // Use SINGLE_BUCKET mode for simpler testing
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(true);

    s3DataStore = new S3DataStore(operator, BUCKET_PREFIX, BUCKET_POSTFIX, DEFAULT_BUCKET_NAME, featureFlagHandler);
  }

  @AfterEach
  void tearDown() {
    if (operator != null) {
      operator.close();
    }
  }

  @Test
  void save() {
    String filePath = FILE_NAME;
    byte[] testData = "test content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);

    String result = s3DataStore.save(filePath, inputStream);

    assertEquals(filePath, result);

    // Verify file was saved by reading it back
    assertTrue(s3DataStore.exists(filePath));
  }

  @Test
  void load() throws IOException {
    String filePath = FILE_NAME;
    byte[] testData = "test content for load".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);

    // First save the file
    s3DataStore.save(filePath, inputStream);

    // Then load it
    InputStream loaded = s3DataStore.load(filePath);

    assertNotNull(loaded);
    byte[] loadedData = loaded.readAllBytes();
    assertArrayEquals(testData, loadedData);
  }

  @Test
  void delete() {
    String filePath = FILE_NAME;
    byte[] testData = "test content for delete".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);

    // First save the file
    s3DataStore.save(filePath, inputStream);
    assertTrue(s3DataStore.exists(filePath));

    // Then delete it
    s3DataStore.delete(filePath);

    // Verify it was deleted
    assertFalse(s3DataStore.exists(filePath));
  }
}
