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

package com.epam.reportportal.base.core.configs;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.apache.opendal.Operator;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

/**
 * Supplies an AWS S3 OpenDAL {@link Operator}, rebuilding it from freshly resolved IAM credentials shortly before they
 * expire.
 *
 * <p>OpenDAL's {@link Operator} is immutable once built, so it cannot pick up rotated IAM-role
 * credentials on its own the way the AWS SDK's own clients do. This mirrors the credential resolution the AWS SDK v2
 * {@link DefaultCredentialsProvider} chain performs (env vars, IRSA web-identity token, EC2/ECS instance metadata) --
 * proven to resolve correctly against this environment -- and additionally forwards the session token, without which S3
 * rejects requests signed with a temporary (STS-issued) access key.
 */
class AwsS3OperatorSupplier implements Supplier<Operator> {

  private static final String ACCESS_KEY_ID = "access_key_id";
  private static final String SECRET_ACCESS_KEY = "secret_access_key";
  private static final String SESSION_TOKEN = "session_token";
  private static final String REGION = "region";
  private static final String BUCKET = "bucket";
  private static final Duration REFRESH_BEFORE_EXPIRY = Duration.ofMinutes(60);

  private final DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();
  private final String region;
  private final String bucket;
  private final Lock lock = new ReentrantLock();

  private volatile Operator cachedOperator;
  private volatile Instant expirationTime = Instant.MIN;

  AwsS3OperatorSupplier(String region, String bucket) {
    this.region = region;
    this.bucket = bucket;
  }

  @Override
  public Operator get() {
    if (needsRefresh()) {
      lock.lock();
      try {
        if (needsRefresh()) {
          refresh();
        }
      } finally {
        lock.unlock();
      }
    }
    return cachedOperator;
  }

  private boolean needsRefresh() {
    return cachedOperator == null || Instant.now().isAfter(expirationTime.minus(REFRESH_BEFORE_EXPIRY));
  }

  private void refresh() {
    AwsCredentials credentials = credentialsProvider.resolveCredentials();

    Map<String, String> config = new HashMap<>();
    config.put(REGION, region);
    config.put(BUCKET, bucket);
    config.put(ACCESS_KEY_ID, credentials.accessKeyId());
    config.put(SECRET_ACCESS_KEY, credentials.secretAccessKey());

    if (credentials instanceof AwsSessionCredentials sessionCredentials) {
      config.put(SESSION_TOKEN, sessionCredentials.sessionToken());
      expirationTime = sessionCredentials.expirationTime().orElseGet(() -> Instant.now().plus(REFRESH_BEFORE_EXPIRY));
    } else {
      expirationTime = Instant.MAX;
    }

    cachedOperator = Operator.of("s3", config);
  }
}
