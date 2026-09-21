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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.apache.opendal.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class DataStoreConfigurationTest {

  private final DataStoreConfiguration config = new DataStoreConfiguration();

  @Test
  @DisplayName("S3 operator passes endpoint config when Cloudflare R2 endpoint is provided")
  void awsS3OperatorWithCloudflareR2Endpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "auto";
    String endpoint = "https://r2.cloudflarestorage.com";
    String bucket = "test-bucket";

    try (MockedStatic<Operator> operatorMock = mockStatic(Operator.class)) {
      Operator mockOperator = mock(Operator.class);
      operatorMock.when(() -> Operator.of(eq("s3"), any(Map.class))).thenReturn(mockOperator);

      Operator result = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

      // Verify operator was created
      assertNotNull(result, "Operator should be created and returned");
      assertEquals(mockOperator, result, "Returned operator should be the one created by Operator.of()");

      // Verify Operator.of() was called exactly once with correct scheme
      ArgumentCaptor<Map<String, String>> configCaptor = ArgumentCaptor.forClass(Map.class);
      operatorMock.verify(() -> Operator.of(eq("s3"), configCaptor.capture()), times(1));

      // Verify endpoint configuration is actually propagated
      Map<String, String> capturedConfig = configCaptor.getValue();
      assertNotNull(capturedConfig, "Configuration map should not be null");
      assertEquals(endpoint, capturedConfig.get("endpoint"),
          "Endpoint should be passed to operator configuration");
      assertEquals(bucket, capturedConfig.get("bucket"),
          "Bucket should be passed to operator configuration");
      assertEquals(region, capturedConfig.get("region"),
          "Region should be passed to operator configuration");
      assertEquals(accessKey, capturedConfig.get("access_key_id"),
          "Access key should be passed to operator configuration");
      assertEquals(secretKey, capturedConfig.get("secret_access_key"),
          "Secret key should be passed to operator configuration");
    }
  }

  @Test
  @DisplayName("S3 operator omits endpoint config when not provided for AWS S3")
  void awsS3OperatorWithoutEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-west-2";
    String endpoint = "";
    String bucket = "test-bucket";

    try (MockedStatic<Operator> operatorMock = mockStatic(Operator.class)) {
      Operator mockOperator = mock(Operator.class);
      operatorMock.when(() -> Operator.of(eq("s3"), any(Map.class))).thenReturn(mockOperator);

      Operator result = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

      assertNotNull(result, "Operator should be created");

      ArgumentCaptor<Map<String, String>> configCaptor = ArgumentCaptor.forClass(Map.class);
      operatorMock.verify(() -> Operator.of(eq("s3"), configCaptor.capture()), times(1));

      Map<String, String> capturedConfig = configCaptor.getValue();
      assertFalse(capturedConfig.containsKey("endpoint"),
          "Endpoint should not be included when empty");
      assertTrue(capturedConfig.containsKey("bucket"),
          "Bucket should always be included in configuration");
      assertTrue(capturedConfig.containsKey("region"),
          "Region should always be included in configuration");
      assertEquals(bucket, capturedConfig.get("bucket"));
      assertEquals(region, capturedConfig.get("region"));
    }
  }

  @Test
  @DisplayName("S3 operator passes MinIO endpoint correctly")
  void awsS3OperatorWithMinioEndpoint() {
    String accessKey = "minioadmin";
    String secretKey = "minioadmin";
    String region = "us-east-1";
    String endpoint = "https://minio.example.com:9000";
    String bucket = "reportportal";

    try (MockedStatic<Operator> operatorMock = mockStatic(Operator.class)) {
      Operator mockOperator = mock(Operator.class);
      operatorMock.when(() -> Operator.of(eq("s3"), any(Map.class))).thenReturn(mockOperator);

      Operator result = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

      assertNotNull(result, "Operator should be created with MinIO endpoint");

      ArgumentCaptor<Map<String, String>> configCaptor = ArgumentCaptor.forClass(Map.class);
      operatorMock.verify(() -> Operator.of(eq("s3"), configCaptor.capture()), times(1));

      Map<String, String> capturedConfig = configCaptor.getValue();
      assertEquals(endpoint, capturedConfig.get("endpoint"),
          "MinIO endpoint should be passed to configuration");
      assertTrue(capturedConfig.containsKey("bucket"), "Bucket configuration must be present");
      assertTrue(capturedConfig.containsKey("region"), "Region configuration must be present");
      assertEquals(bucket, capturedConfig.get("bucket"));
      assertEquals(region, capturedConfig.get("region"));
    }
  }

  @Test
  @DisplayName("S3 operator omits credentials when empty (IAM role mode)")
  void awsS3OperatorWithIamRole() {
    String accessKey = "";
    String secretKey = "";
    String region = "us-east-1";
    String endpoint = "";
    String bucket = "test-bucket";

    try (MockedStatic<Operator> operatorMock = mockStatic(Operator.class)) {
      Operator mockOperator = mock(Operator.class);
      operatorMock.when(() -> Operator.of(eq("s3"), any(Map.class))).thenReturn(mockOperator);

      Operator result = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

      assertNotNull(result, "Operator should be created with IAM role credentials");

      ArgumentCaptor<Map<String, String>> configCaptor = ArgumentCaptor.forClass(Map.class);
      operatorMock.verify(() -> Operator.of(eq("s3"), configCaptor.capture()), times(1));

      Map<String, String> capturedConfig = configCaptor.getValue();
      assertFalse(capturedConfig.containsKey("access_key_id"),
          "Credentials should not be included when empty");
      assertFalse(capturedConfig.containsKey("secret_access_key"),
          "Secret should not be included when empty");
      assertTrue(capturedConfig.containsKey("bucket"), "Bucket must always be configured");
      assertTrue(capturedConfig.containsKey("region"), "Region must always be configured");
    }
  }

  @Test
  @DisplayName("S3-compatible operator passes endpoint configuration")
  void s3CompatibleOperatorWithMinioEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String endpoint = "https://minio.example.com";
    String region = "us-east-1";
    String bucket = "test-bucket";

    try (MockedStatic<Operator> operatorMock = mockStatic(Operator.class)) {
      Operator mockOperator = mock(Operator.class);
      operatorMock.when(() -> Operator.of(eq("s3"), any(Map.class))).thenReturn(mockOperator);

      Operator result = config.s3CompatibleOperator(accessKey, secretKey, endpoint, region, bucket);

      assertNotNull(result, "Operator should be created with MinIO endpoint");
      assertEquals(mockOperator, result, "Returned operator should be the one created by OpenDAL");

      ArgumentCaptor<Map<String, String>> configCaptor = ArgumentCaptor.forClass(Map.class);
      operatorMock.verify(() -> Operator.of(eq("s3"), configCaptor.capture()), times(1));

      Map<String, String> capturedConfig = configCaptor.getValue();
      assertEquals(endpoint, capturedConfig.get("endpoint"),
          "S3-compatible operator should include endpoint");
      assertEquals(accessKey, capturedConfig.get("access_key_id"),
          "Access key should be configured");
      assertEquals(secretKey, capturedConfig.get("secret_access_key"),
          "Secret key should be configured");
      assertEquals(bucket, capturedConfig.get("bucket"),
          "Bucket should be configured");
      assertEquals(region, capturedConfig.get("region"),
          "Region should be configured");
    }
  }

  @Test
  @DisplayName("Endpoint propagation is verified through actual Operator.of() invocation")
  void endpointPropagationVerifiedThroughOperatorFactory() {
    String endpoint = "https://s3.custom-provider.com";
    String bucket = "test-bucket";
    String region = "us-east-1";
    String accessKey = "key";
    String secretKey = "secret";

    try (MockedStatic<Operator> operatorMock = mockStatic(Operator.class)) {
      Operator mockOperator = mock(Operator.class);
      operatorMock.when(() -> Operator.of(eq("s3"), any(Map.class))).thenReturn(mockOperator);

      config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

      // Verify that Operator.of() was called with a configuration containing the endpoint
      ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
      operatorMock.verify(() -> Operator.of(eq("s3"), captor.capture()));

      Map<String, String> configUsedByOpenDAL = captor.getValue();
      assertTrue(configUsedByOpenDAL.containsKey("endpoint"),
          "Configuration passed to OpenDAL must include endpoint");
      assertEquals(endpoint, configUsedByOpenDAL.get("endpoint"),
          "Endpoint value must be correctly propagated to OpenDAL");
    }
  }
}
