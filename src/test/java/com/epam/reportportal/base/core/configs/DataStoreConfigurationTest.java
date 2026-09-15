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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.opendal.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataStoreConfigurationTest {

  private final DataStoreConfiguration config = new DataStoreConfiguration();

  @Test
  @DisplayName("S3 operator includes endpoint when provided")
  void awsS3OperatorWithEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-east-1";
    String endpoint = "https://r2.cloudflarestorage.com";
    String bucket = "test-bucket";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator);
  }

  @Test
  @DisplayName("S3 operator works without endpoint for AWS S3")
  void awsS3OperatorWithoutEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-west-2";
    String endpoint = "";
    String bucket = "test-bucket";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator);
  }

  @Test
  @DisplayName("S3 operator works with empty credentials")
  void awsS3OperatorWithoutCredentials() {
    String accessKey = "";
    String secretKey = "";
    String region = "us-east-1";
    String endpoint = "";
    String bucket = "test-bucket";

    Operator operator = config.awsS3Operator(accessKey, secretKey, region, endpoint, bucket);

    assertNotNull(operator);
  }

  @Test
  @DisplayName("S3-compatible operator includes endpoint")
  void s3CompatibleOperatorWithEndpoint() {
    String accessKey = "test-access-key";
    String secretKey = "test-secret-key";
    String endpoint = "https://minio.example.com";
    String region = "us-east-1";
    String bucket = "test-bucket";

    Operator operator = config.s3CompatibleOperator(accessKey, secretKey, endpoint, region, bucket);

    assertNotNull(operator);
  }
}
