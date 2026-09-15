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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.opendal.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataStoreConfigurationTest {

  private final DataStoreConfiguration config = new DataStoreConfiguration();

  @Test
  @DisplayName("S3 operator successfully creates with custom endpoint for Cloudflare R2")
  void awsS3OperatorWithCloudflareR2Endpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "auto";
    String endpoint = "https://r2.cloudflarestorage.com";
    String bucket = "test-bucket";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator, "Operator should be created with Cloudflare R2 endpoint");
  }

  @Test
  @DisplayName("S3 operator successfully creates without endpoint for standard AWS S3")
  void awsS3OperatorWithoutEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-west-2";
    String endpoint = "";
    String bucket = "test-bucket";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator, "Operator should be created for standard AWS S3 without endpoint");
  }

  @Test
  @DisplayName("S3 operator successfully creates with MinIO endpoint")
  void awsS3OperatorWithMinioEndpoint() {
    String accessKey = "minioadmin";
    String secretKey = "minioadmin";
    String region = "us-east-1";
    String endpoint = "https://minio.example.com:9000";
    String bucket = "reportportal";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator, "Operator should be created with MinIO endpoint");
  }

  @Test
  @DisplayName("S3 operator works with IAM role credentials (empty key/secret)")
  void awsS3OperatorWithIamRole() {
    String accessKey = "";
    String secretKey = "";
    String region = "us-east-1";
    String endpoint = "";
    String bucket = "test-bucket";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator, "Operator should be created with IAM role credentials");
  }

  @Test
  @DisplayName("S3-compatible operator creates with MinIO endpoint")
  void s3CompatibleOperatorWithMinioEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String endpoint = "https://minio.example.com";
    String region = "us-east-1";
    String bucket = "test-bucket";

    Operator operator = config.s3CompatibleOperator(accessKey, secretKey, endpoint, region, bucket);

    assertNotNull(operator, "S3-compatible operator should be created with MinIO endpoint");
  }
}
