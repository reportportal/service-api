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

import com.epam.reportportal.base.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.base.infrastructure.commons.Thumbnailator;
import com.epam.reportportal.base.infrastructure.commons.ThumbnailatorImpl;
import com.epam.reportportal.base.infrastructure.commons.TikaContentTypeResolver;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStoreClient;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.tms.LocalTmsDataStore;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.tms.TmsDataStore;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.opendal.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Dzianis_Shybeka
 */
@Configuration
public class DataStoreConfiguration {

  private static final String ACCESS_KEY_ID = "access_key_id";
  private static final String SECRET_ACCESS_KEY = "secret_access_key";
  private static final String BUCKET = "bucket";
  private static final String ENDPOINT = "endpoint";
  private static final String REGION = "region";
  private static final String ROOT = "root";

  /**
   * Creates OpenDAL Operator for the local filesystem.
   *
   * @param baseDirectory root directory to store files under
   * @return {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "filesystem")
  @Primary
  public Operator filesystemOperator(@Value("${datastore.path:/data/store}") String baseDirectory) {
    return fsOperator(baseDirectory);
  }

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "filesystem")
  public DataStore localDataStore(@Autowired Operator operator,
      FeatureFlagHandler featureFlagHandler,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName) {
    return new DataStoreClient(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates OpenDAL Operator for MinIO.
   *
   * @param accessKey accessKey to use
   * @param secretKey secretKey to use
   * @param endpoint  MinIO endpoint
   * @param region    Region to use
   * @return {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "s3-compatible")
  @Primary
  public Operator s3CompatibleOperator(@Value("${datastore.accessKey}") String accessKey,
      @Value("${datastore.secretKey}") String secretKey,
      @Value("${datastore.endpoint}") String endpoint,
      @Value("${datastore.region:us-east-1}") String region,
      @Value("${datastore.defaultBucketName}") String defaultBucketName) {

    Map<String, String> config = new HashMap<>();
    config.put(ACCESS_KEY_ID, accessKey);
    config.put(SECRET_ACCESS_KEY, secretKey);
    config.put(ENDPOINT, endpoint);
    config.put(REGION, region);
    config.put(BUCKET, defaultBucketName);

    return Operator.of("s3", config);
  }

  /**
   * Creates DataStore bean to work with MinIO.
   *
   * @param operator           {@link Operator} object
   * @param bucketPrefix       Prefix for bucket name
   * @param bucketPostfix      Postfix for bucket name
   * @param defaultBucketName  Name of default bucket to use
   * @param featureFlagHandler Instance of {@link FeatureFlagHandler} to check enabled features
   * @return {@link DataStore} object
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "s3-compatible")
  public DataStore s3CompatibleDataStore(@Autowired Operator operator,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName,
      FeatureFlagHandler featureFlagHandler) {
    return new DataStoreClient(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates a {@link Supplier} of OpenDAL {@link Operator} for AWS S3.
   *
   * <p>When no static key is configured, IAM-role credentials are resolved via the AWS SDK
   * (env vars, IRSA web-identity token, EC2/ECS instance metadata) and the {@link Operator} is
   * rebuilt shortly before they expire, since {@link Operator} itself cannot be refreshed in
   * place. See {@link AwsS3OperatorSupplier}.
   *
   * @param accessKey accessKey to use (optional, if not provided uses IAM-role credentials)
   * @param secretKey secretKey to use (optional, see {@code accessKey})
   * @param region    AWS S3 region to use.
   * @return {@link Supplier} of {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "aws-s3")
  public Supplier<Operator> awsS3Operator(
      @Value("${datastore.accessKey:}") String accessKey,
      @Value("${datastore.secretKey:}") String secretKey,
      @Value("${datastore.region}") String region,
      @Value("${datastore.defaultBucketName}") String defaultBucketName) {

    if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)) {
      Map<String, String> config = new HashMap<>();
      config.put(REGION, region);
      config.put(BUCKET, defaultBucketName);
      config.put(ACCESS_KEY_ID, accessKey);
      config.put(SECRET_ACCESS_KEY, secretKey);

      Operator operator = Operator.of("s3", config);
      return () -> operator;
    }

    return new AwsS3OperatorSupplier(region, defaultBucketName);
  }

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "aws-s3")
  public DataStore s3DataStore(@Autowired Supplier<Operator> operator,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName,
      FeatureFlagHandler featureFlagHandler) {
    return new DataStoreClient(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates OpenDAL Operator for Azure Blob Storage.
   *
   * @param accountName Azure account name
   * @param accountKey  Azure account key
   * @param endpoint    Azure endpoint (optional)
   * @param container   Azure container (optional, but usually required for OpenDAL azblob)
   * @return {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "azure")
  @Primary
  public Operator azureBlobOperator(
      @Value("${datastore.azure.accountName}") String accountName,
      @Value("${datastore.azure.accountKey}") String accountKey,
      @Value("${datastore.azure.endpoint:}") String endpoint,
      @Value("${datastore.azure.container:}") String container) {

    Map<String, String> config = new HashMap<>();
    config.put("account_name", accountName);
    config.put("account_key", accountKey);
    if (StringUtils.isNotEmpty(endpoint)) {
      config.put(ENDPOINT, endpoint);
    }
    if (StringUtils.isNotEmpty(container)) {
      config.put("container", container);
    }

    return Operator.of("azblob", config);
  }

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "azure")
  public DataStore azureDataStore(@Autowired Operator operator,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName,
      FeatureFlagHandler featureFlagHandler) {
    // Reusing DataStoreClient as it is generic enough for OpenDAL operations
    return new DataStoreClient(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates OpenDAL Operator for Google Cloud Storage.
   *
   * @param bucket          GCS bucket name to store objects in
   * @param credentialsPath path to a GCS service account JSON key file (optional, if not provided OpenDAL falls back to
   *                        Application Default Credentials)
   * @param endpoint        GCS endpoint (optional)
   * @return {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "gcs")
  @Primary
  public Operator gcsOperator(
      @Value("${datastore.gcs.bucket}") String bucket,
      @Value("${datastore.gcs.credentialsPath:}") String credentialsPath,
      @Value("${datastore.gcs.endpoint:}") String endpoint) {

    Map<String, String> config = new HashMap<>();
    config.put(BUCKET, bucket);
    if (StringUtils.isNotEmpty(credentialsPath)) {
      config.put("credential_path", credentialsPath);
    }
    if (StringUtils.isNotEmpty(endpoint)) {
      config.put(ENDPOINT, endpoint);
    }

    return Operator.of("gcs", config);
  }

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "gcs")
  public DataStore gcsDataStore(@Autowired Operator operator,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName,
      FeatureFlagHandler featureFlagHandler) {
    // Reusing DataStoreClient as it is generic enough for OpenDAL operations
    return new DataStoreClient(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates OpenDAL Operator for the local filesystem, used by the TMS data store.
   *
   * @param baseDirectory root directory to store TMS files under
   * @return {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "rp.tms.datastore.type", havingValue = "filesystem")
  public Operator tmsFilesystemOperator(@Value("${rp.tms.datastore.path:/data/store}") String baseDirectory) {
    return fsOperator(baseDirectory);
  }

  @Bean
  @ConditionalOnProperty(name = "rp.tms.datastore.type", havingValue = "filesystem")
  public TmsDataStore tmsLocalDataStore(
      @Autowired @Qualifier("tmsFilesystemOperator") Operator tmsFilesystemOperator,
      FeatureFlagHandler featureFlagHandler,
      @Value("${rp.tms.datastore.bucketPrefix:tms-prj-}") String bucketPrefix,
      @Value("${rp.tms.datastore.bucketPostfix:}") String bucketPostfix,
      @Value("${rp.tms.datastore.defaultBucketName:tms-rp-bucket}") String defaultBucketName) {
    return new LocalTmsDataStore(
        tmsFilesystemOperator, featureFlagHandler, bucketPrefix, bucketPostfix, defaultBucketName);
  }

  @Bean("attachmentThumbnailator")
  public Thumbnailator attachmentThumbnailator(
      @Value("${datastore.thumbnail.attachment.width}") int width,
      @Value("${datastore.thumbnail.attachment.height}") int height) {
    return new ThumbnailatorImpl(width, height);
  }

  @Bean("userPhotoThumbnailator")
  public Thumbnailator userPhotoThumbnailator(
      @Value("${datastore.thumbnail.avatar.width}") int width,
      @Value("${datastore.thumbnail.avatar.height}") int height) {
    return new ThumbnailatorImpl(width, height);
  }

  @Bean
  public ContentTypeResolver contentTypeResolver() {
    return new TikaContentTypeResolver();
  }

  private Operator fsOperator(String baseDirectory) {
    Map<String, String> config = new HashMap<>();
    config.put(ROOT, baseDirectory);

    return Operator.of("fs", config);
  }
}
