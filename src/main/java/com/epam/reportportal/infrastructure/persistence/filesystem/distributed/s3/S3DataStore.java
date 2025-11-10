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

package com.epam.reportportal.infrastructure.persistence.filesystem.distributed.s3;

import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implimitation of basic operations with blob storages.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class S3DataStore implements DataStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3DataStore.class);
  private static final Lock CREATE_BUCKET_LOCK = new ReentrantLock();

  private final BlobStore blobStore;
  private final String bucketPrefix;
  private final String bucketPostfix;
  private final String defaultBucketName;
  private final Location location;

  private final FeatureFlagHandler featureFlagHandler;

  /**
   * Initialises {@link S3DataStore}.
   *
   * @param blobStore          {@link BlobStore}
   * @param bucketPrefix       Prefix for bucket name
   * @param bucketPostfix      Postfix for bucket name
   * @param defaultBucketName  Name of default bucket to use
   * @param region             Region to use
   * @param featureFlagHandler {@link FeatureFlagHandler}
   */
  public S3DataStore(BlobStore blobStore, String bucketPrefix, String bucketPostfix,
      String defaultBucketName, String region, FeatureFlagHandler featureFlagHandler) {
    this.blobStore = blobStore;
    this.bucketPrefix = bucketPrefix;
    this.bucketPostfix = Objects.requireNonNullElse(bucketPostfix, "");
    this.defaultBucketName = defaultBucketName;
    this.location = getLocationFromString(region);
    this.featureFlagHandler = featureFlagHandler;
  }

  @Override
  public String save(String filePath, InputStream inputStream) {
    if (filePath == null) {
      return "";
    }
    StoredFile storedFile = getStoredFile(filePath);
    try {
      if (!blobStore.containerExists(storedFile.getBucket())) {
        CREATE_BUCKET_LOCK.lock();
        try {
          if (!blobStore.containerExists(storedFile.getBucket())) {
            blobStore.createContainerInLocation(location, storedFile.getBucket());
          }
        } finally {
          CREATE_BUCKET_LOCK.unlock();
        }
      }

      Blob objectBlob = blobStore.blobBuilder(storedFile.getFilePath()).payload(inputStream)
          .contentDisposition(storedFile.getFilePath()).contentLength(inputStream.available())
          .build();
      blobStore.putBlob(storedFile.getBucket(), objectBlob);
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
    Blob fileBlob = blobStore.getBlob(storedFile.getBucket(), storedFile.getFilePath());
    if (fileBlob != null) {
      try {
        return fileBlob.getPayload().openStream();
      } catch (IOException e) {
        throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
      }
    }
    LOGGER.error("Unable to find file '{}'", filePath);
    throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
  }

  @Override
  public boolean exists(String filePath) {
    if (filePath == null) {
      return false;
    }
    StoredFile storedFile = getStoredFile(filePath);
    return blobStore.blobExists(storedFile.getBucket(), storedFile.getFilePath());
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
      LOGGER.error("Unable to delete file '{}'", filePath, e);
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to delete file");
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
    if (!featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      blobStore.deleteContainer(bucketPrefix + bucketName + bucketPostfix);
    } else {
      blobStore.deleteContainer(bucketName);
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

  private Location getLocationFromString(String locationString) {
    Location location = null;
    if (locationString != null) {
      location =
          new LocationBuilder().scope(LocationScope.REGION).id(locationString).description("region")
              .build();
    }
    return location;
  }

  private String retrievePath(Path path, int beginIndex, int endIndex) {
    return String.valueOf(path.subpath(beginIndex, endIndex));
  }
}
