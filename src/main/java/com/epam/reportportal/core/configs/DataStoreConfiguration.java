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

package com.epam.reportportal.core.configs;

import com.epam.reportportal.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.infrastructure.commons.Thumbnailator;
import com.epam.reportportal.infrastructure.commons.ThumbnailatorImpl;
import com.epam.reportportal.infrastructure.commons.TikaContentTypeResolver;
import com.epam.reportportal.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.infrastructure.persistence.filesystem.LocalDataStore;
import com.epam.reportportal.infrastructure.persistence.filesystem.distributed.s3.S3DataStore;
import com.epam.reportportal.infrastructure.persistence.filesystem.tms.LocalTmsDataStore;
import com.epam.reportportal.infrastructure.persistence.filesystem.tms.TmsDataStore;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.opendal.Operator;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

/**
 * @author Dzianis_Shybeka
 */
@Configuration
public class DataStoreConfiguration {

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "filesystem")
  @Primary
  public BlobStore filesystemBlobStore(
      @Value("${datastore.path:/data/store}") String baseDirectory) {

    Properties properties = new Properties();
    properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, baseDirectory);

    BlobStoreContext blobStoreContext =
        ContextBuilder.newBuilder("filesystem").overrides(properties)
            .buildView(BlobStoreContext.class);

    return blobStoreContext.getBlobStore();
  }

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "filesystem")
  public DataStore localDataStore(@Autowired BlobStore blobStore,
      FeatureFlagHandler featureFlagHandler,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName) {
    return new LocalDataStore(
        blobStore, featureFlagHandler, bucketPrefix, bucketPostfix, defaultBucketName);
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
      @Value("${datastore.region:us-east-1}") String region) {

    Map<String, String> config = new HashMap<>();
    config.put("access_key_id", accessKey);
    config.put("secret_access_key", secretKey);
    config.put("endpoint", endpoint);
    config.put("region", region);

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
    return new S3DataStore(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates OpenDAL Operator for AWS S3.
   *
   * @param accessKey accessKey to use (optional, if not provided uses IAM credentials)
   * @param secretKey secretKey to use (optional, if not provided uses IAM credentials)
   * @param region    AWS S3 region to use.
   * @return {@link Operator}
   */
  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "aws-s3")
  @Primary
  public Operator awsS3Operator(
      @Value("${datastore.accessKey:}") String accessKey,
      @Value("${datastore.secretKey:}") String secretKey,
      @Value("${datastore.region}") String region) {

    Map<String, String> config = new HashMap<>();
    config.put("region", region);

    if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)) {
      config.put("access_key_id", accessKey);
      config.put("secret_access_key", secretKey);
    } else {
      // Use IAM credentials from DefaultCredentialsProvider
      DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
      AwsCredentials awsCredentials = credentialsProvider.resolveCredentials();
      config.put("access_key_id", awsCredentials.accessKeyId());
      config.put("secret_access_key", awsCredentials.secretAccessKey());
    }

    return Operator.of("s3", config);
  }

  @Bean
  @ConditionalOnProperty(name = "datastore.type", havingValue = "aws-s3")
  public DataStore s3DataStore(@Autowired Operator operator,
      @Value("${datastore.bucketPrefix}") String bucketPrefix,
      @Value("${datastore.bucketPostfix}") String bucketPostfix,
      @Value("${datastore.defaultBucketName}") String defaultBucketName,
      FeatureFlagHandler featureFlagHandler) {
    return new S3DataStore(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Creates OpenDAL Operator for Azure Blob Storage.
   *
   * @param accountName   Azure account name
   * @param accountKey    Azure account key
   * @param endpoint      Azure endpoint (optional)
   * @param container     Azure container (optional, but usually required for OpenDAL azblob)
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
      config.put("endpoint", endpoint);
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
    // Reusing S3DataStore as it is generic enough for OpenDAL operations
    return new S3DataStore(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  @Bean
  @ConditionalOnProperty(name = "rp.tms.datastore.type", havingValue = "filesystem")
  public BlobStore tmsFilesystemBlobStore(
      @Value("${rp.tms.datastore.path:/data/store}") String baseDirectory) {

    Properties properties = new Properties();
    properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, baseDirectory);

    BlobStoreContext blobStoreContext =
        ContextBuilder.newBuilder("filesystem").overrides(properties)
            .buildView(BlobStoreContext.class);

    return blobStoreContext.getBlobStore();
  }

  @Bean
  @ConditionalOnProperty(name = "rp.tms.datastore.type", havingValue = "filesystem")
  public TmsDataStore tmsLocalDataStore(
      @Autowired @Qualifier("tmsFilesystemBlobStore") BlobStore tmsFilesystemBlobStore,
      FeatureFlagHandler featureFlagHandler,
      @Value("${rp.tms.datastore.bucketPrefix:tms-prj-}") String bucketPrefix,
      @Value("${rp.tms.datastore.bucketPostfix:}") String bucketPostfix,
      @Value("${rp.tms.datastore.defaultBucketName:tms-rp-bucket}") String defaultBucketName) {
    return new LocalTmsDataStore(
        tmsFilesystemBlobStore, featureFlagHandler, bucketPrefix, bucketPostfix, defaultBucketName);
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
}
