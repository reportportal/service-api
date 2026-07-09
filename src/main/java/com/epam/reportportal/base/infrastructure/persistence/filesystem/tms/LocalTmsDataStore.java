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

package com.epam.reportportal.base.infrastructure.persistence.filesystem.tms;

import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStoreClient;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import java.io.InputStream;
import java.util.List;
import org.apache.opendal.Operator;

/**
 * Local file-system implementation of the TMS data store, backed by Apache OpenDAL.
 *
 * <p>Delegates to {@link DataStoreClient}, since {@link TmsDataStore} and {@link DataStore}
 * share the same contract.
 *
 * @author Dzianis_Shybeka
 */
public class LocalTmsDataStore implements TmsDataStore {

  private final DataStore delegate;

  public LocalTmsDataStore(Operator operator, FeatureFlagHandler featureFlagHandler,
      String bucketPrefix, String bucketPostfix, String defaultBucketName) {
    this.delegate =
        new DataStoreClient(operator, bucketPrefix, bucketPostfix, defaultBucketName, featureFlagHandler);
  }

  @Override
  public String save(String filePath, InputStream inputStream) {
    return delegate.save(filePath, inputStream);
  }

  @Override
  public InputStream load(String filePath) {
    return delegate.load(filePath);
  }

  @Override
  public boolean exists(String filePath) {
    return delegate.exists(filePath);
  }

  @Override
  public void delete(String filePath) {
    delegate.delete(filePath);
  }

  @Override
  public void deleteAll(List<String> filePaths, String bucketName) {
    delegate.deleteAll(filePaths, bucketName);
  }

  @Override
  public void deleteContainer(String bucketName) {
    delegate.deleteContainer(bucketName);
  }
}
