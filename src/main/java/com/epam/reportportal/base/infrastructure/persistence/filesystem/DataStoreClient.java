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

package com.epam.reportportal.base.infrastructure.persistence.filesystem;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.opendal.Operator;

/**
 * Implementation of basic operations with blob storages using Apache OpenDAL.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Slf4j
public class DataStoreClient implements DataStore {

  private static final String PATH_DELIMITER = "/";

  private final Supplier<Operator> operatorSupplier;
  private final String bucketPrefix;
  private final String bucketPostfix;
  private final String defaultBucketName;

  private final FeatureFlagHandler featureFlagHandler;

  /**
   * Initialises {@link DataStoreClient}.
   *
   * @param operator           {@link Operator} OpenDAL operator instance
   * @param bucketPrefix       Prefix for bucket name
   * @param bucketPostfix      Postfix for bucket name
   * @param defaultBucketName  Name of default bucket to use
   * @param featureFlagHandler {@link FeatureFlagHandler}
   */
  public DataStoreClient(Operator operator, String bucketPrefix, String bucketPostfix,
      String defaultBucketName, FeatureFlagHandler featureFlagHandler) {
    this(() -> operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  /**
   * Initialises {@link DataStoreClient} with an {@link Operator} that may need to be rebuilt over
   * the client's lifetime, e.g. when backed by rotating IAM credentials.
   *
   * @param operatorSupplier   supplies the current {@link Operator} to use for each operation
   * @param bucketPrefix       Prefix for bucket name
   * @param bucketPostfix      Postfix for bucket name
   * @param defaultBucketName  Name of default bucket to use
   * @param featureFlagHandler {@link FeatureFlagHandler}
   */
  public DataStoreClient(Supplier<Operator> operatorSupplier, String bucketPrefix, String bucketPostfix,
      String defaultBucketName, FeatureFlagHandler featureFlagHandler) {
    this.operatorSupplier = operatorSupplier;
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
      byte[] data = inputStream.readAllBytes();
      String fullPath = getFullPath(storedFile);
      operatorSupplier.get().write(fullPath, data);
      return Paths.get(filePath).toString();
    } catch (IOException e) {
      log.error("Unable to save file '{}'", filePath, e);
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save file");
    }
  }

  @Override
  public InputStream load(String filePath) {
    if (filePath == null) {
      log.error("Unable to find file");
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
    }
    StoredFile storedFile = getStoredFile(filePath);
    String fullPath = getFullPath(storedFile);
    try {
      byte[] data = operatorSupplier.get().read(fullPath);
      return new ByteArrayInputStream(data);
    } catch (Exception e) {
      log.error("Unable to find file '{}'", filePath, e);
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to find file");
    }
  }

  @Override
  public boolean exists(String filePath) {
    if (filePath == null) {
      return false;
    }
    StoredFile storedFile = getStoredFile(filePath);
    String fullPath = getFullPath(storedFile);
    try {
      operatorSupplier.get().stat(fullPath);
      return true;
    } catch (Exception _) {
      return false;
    }
  }

  @Override
  public void delete(String filePath) {
    if (filePath == null) {
      return;
    }
    StoredFile storedFile = getStoredFile(filePath);
    String fullPath = getFullPath(storedFile);
    try {
      operatorSupplier.get().delete(fullPath);
    } catch (Exception e) {
      log.error("Unable to delete file '{}'", filePath, e);
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
        String fullPath = featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)
            ? filePath
            : bucket + PATH_DELIMITER + filePath;
        operatorSupplier.get().delete(fullPath);
      } catch (Exception e) {
        log.error("Unable to delete file '{}' from bucket '{}'", filePath, bucket, e);
      }
    }
  }

  @Override
  public void deleteContainer(String bucketName) {
    String bucket = featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)
        ? bucketName
        : bucketPrefix + bucketName + bucketPostfix;

    try {
      String path = featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)
          ? PATH_DELIMITER
          : bucket + PATH_DELIMITER;
      operatorSupplier.get().removeAll(path);
    } catch (Exception e) {
      log.error("Unable to delete container '{}'", bucket, e);
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

  private String getFullPath(StoredFile storedFile) {
    if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      return storedFile.filePath();
    }
    return storedFile.bucket() + PATH_DELIMITER + storedFile.filePath();
  }

  private String retrievePath(Path path, int beginIndex, int endIndex) {
    return String.valueOf(path.subpath(beginIndex, endIndex));
  }
}
