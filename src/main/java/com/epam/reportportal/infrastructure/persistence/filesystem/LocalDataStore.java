/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.infrastructure.persistence.filesystem;

import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.filesystem.distributed.s3.StoredFile;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dzianis_Shybeka
 */
public class LocalDataStore implements DataStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalDataStore.class);
  private final BlobStore blobStore;

  private final FeatureFlagHandler featureFlagHandler;

  private final String bucketPrefix;

  private final String bucketPostfix;

  private final String defaultBucketName;

  public LocalDataStore(BlobStore blobStore, FeatureFlagHandler featureFlagHandler,
      String bucketPrefix, String bucketPostfix, String defaultBucketName) {
    this.blobStore = blobStore;
    this.featureFlagHandler = featureFlagHandler;
    this.bucketPrefix = bucketPrefix;
    this.bucketPostfix = Objects.requireNonNullElse(bucketPostfix, "");
    this.defaultBucketName = defaultBucketName;
  }

  @Override
  public String save(String filePath, InputStream inputStream) {
    if (filePath == null) {
      return "";
    }
    StoredFile storedFile = getStoredFile(filePath);
    try {
      if (!blobStore.containerExists(storedFile.getBucket())) {
        blobStore.createContainerInLocation(null, storedFile.getBucket());
      }
      Blob objectBlob = blobStore.blobBuilder(storedFile.getFilePath()).payload(inputStream)
          .contentDisposition(storedFile.getFilePath()).contentLength(inputStream.available())
          .build();
      blobStore.putBlob(storedFile.getBucket(), objectBlob);
      return filePath;
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save file", e);
    }
  }

  @Override
  public InputStream load(String filePath) {
    if (filePath == null) {
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
    }
    StoredFile storedFile = getStoredFile(filePath);
    Blob fileBlob = blobStore.getBlob(storedFile.getBucket(), storedFile.getFilePath());
    if (fileBlob != null) {
      try {
        return fileBlob.getPayload().openStream();
      } catch (IOException e) {
        throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage(), e);
      }
    }
    throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
  }

  @Override
  public boolean exists(String filePath) {
    if (filePath == null) {
      return false;
    }
    StoredFile storedFile = getStoredFile(filePath);
    if (blobStore.containerExists(storedFile.getBucket())) {
      return blobStore.blobExists(storedFile.getBucket(), storedFile.getFilePath());
    } else {
      LOGGER.warn("Container '{}' does not exist", storedFile.getBucket());
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
      blobStore.removeBlob(storedFile.getBucket(), storedFile.getFilePath());
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to delete file", e);
    }
  }

  @Override
  public void deleteAll(List<String> filePaths, String bucketName) {
    if (!featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      blobStore.removeBlobs(bucketPrefix + bucketName + bucketPostfix, filePaths);
    } else {
      blobStore.removeBlobs(bucketName, filePaths);
    }
  }

  @Override
  public void deleteContainer(String bucketName) {
    blobStore.deleteContainer(bucketName);
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

