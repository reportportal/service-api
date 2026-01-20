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

import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.infrastructure.persistence.filesystem.distributed.s3.StoredFile;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;

import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of DataStore for Google Cloud Storage.
 */
public class GcpDataStore implements DataStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(GcpDataStore.class);

  private final Storage storage;
  private final String bucketPrefix;
  private final String bucketPostfix;
  private final String defaultBucketName;
  private final FeatureFlagHandler featureFlagHandler;

  public GcpDataStore(Storage storage, String bucketPrefix, String bucketPostfix,
      String defaultBucketName, FeatureFlagHandler featureFlagHandler) {
    this.storage = storage;
    this.bucketPrefix = bucketPrefix;
    this.bucketPostfix = Objects.requireNonNullElse(bucketPostfix, "");
    this.defaultBucketName = defaultBucketName;
    this.featureFlagHandler = featureFlagHandler;
  }

  @Override
  public String save(String filePath, InputStream inputStream) {
    if (filePath == null) {
      return "";
    }
    StoredFile storedFile = getStoredFile(filePath);
    try {
      BlobId blobId = BlobId.of(storedFile.getBucket(), storedFile.getFilePath());
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      storage.createFrom(blobInfo, inputStream);
      return Paths.get(filePath).toString();
    } catch (IOException e) {
      LOGGER.error("Unable to save file '{}'", filePath, e);
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save file");
    }
  }

  @Override
  public InputStream load(String filePath) {
    if (filePath == null) {
      LOGGER.error("Unable to find file");
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
    }
    StoredFile storedFile = getStoredFile(filePath);
    try {
      BlobId blobId = BlobId.of(storedFile.getBucket(), storedFile.getFilePath());
      Blob blob = storage.get(blobId);
      if (blob == null) {
          throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
      }
      return new ByteArrayInputStream(blob.getContent());
    } catch (Exception e) {
      LOGGER.error("Unable to find file '{}'", filePath, e);
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
    }
  }

  @Override
  public boolean exists(String filePath) {
    if (filePath == null) {
      return false;
    }
    StoredFile storedFile = getStoredFile(filePath);
    try {
      BlobId blobId = BlobId.of(storedFile.getBucket(), storedFile.getFilePath());
      Blob blob = storage.get(blobId);
      return blob != null && blob.exists();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void delete(String filePath) {
    if (filePath == null) {
      return;
    }
    StoredFile storedFile = getStoredFile(filePath);
    try {
      BlobId blobId = BlobId.of(storedFile.getBucket(), storedFile.getFilePath());
      storage.delete(blobId);
    } catch (Exception e) {
      LOGGER.error("Unable to delete file '{}'", filePath, e);
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to delete file");
    }
  }

  @Override
  public void deleteAll(List<String> filePaths, String bucketName) {
    String bucket = featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)
        ? bucketName
        : bucketPrefix + bucketName + bucketPostfix;

    for (String filePath : filePaths) {
      try {
        BlobId blobId = BlobId.of(bucket, filePath);
        storage.delete(blobId);
      } catch (Exception e) {
        LOGGER.error("Unable to delete file '{}' from bucket '{}'", filePath, bucket, e);
      }
    }
  }

  @Override
  public void deleteContainer(String bucketName) {
    String bucket = featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)
        ? bucketName
        : bucketPrefix + bucketName + bucketPostfix;

    try {
        Iterable<Blob> blobs = storage.list(bucket).iterateAll();
        for (Blob blob : blobs) {
            blob.delete();
        }
    } catch (Exception e) {
      LOGGER.error("Unable to delete container '{}'", bucket, e);
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to delete container");
    }
  }

  private StoredFile getStoredFile(String filePath) {
    if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      return new StoredFile(defaultBucketName, filePath);
    }
    Path targetPath = Paths.get(filePath);
    int nameCount = targetPath.getNameCount();
    String bucketName;
    if (nameCount > 1) {
      bucketName = bucketPrefix + retrievePath(targetPath, 0, 1) + bucketPostfix;
      return new StoredFile(bucketName, retrievePath(targetPath, 1, nameCount));
    } else {
      bucketName = defaultBucketName;
      return new StoredFile(bucketName, retrievePath(targetPath, 0, 1));
    }
  }

  private String retrievePath(Path path, int beginIndex, int endIndex) {
    return String.valueOf(path.subpath(beginIndex, endIndex));
  }
}
