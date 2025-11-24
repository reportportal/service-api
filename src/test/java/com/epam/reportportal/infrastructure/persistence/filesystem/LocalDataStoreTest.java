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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import java.io.InputStream;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.io.Payload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LocalDataStoreTest {

  private LocalDataStore localDataStore;

  private BlobStore blobStore;

  private FeatureFlagHandler featureFlagHandler;

  private final InputStream inputStream = mock(InputStream.class);

  private static final int ZERO = 0;

  private static final String FILE_PATH = "someFile.txt";

  private static final String MULTI_BUCKET_NAME = "multiBucket";

  private static final String BUCKET_PREFIX = "prj-";

  private static final String BUCKET_POSTFIX = "-tiest";

  private static final String DEFAULT_BUCKET_NAME = "rp-bucket";

  private static final String MULTI_FILE_PATH = MULTI_BUCKET_NAME + "/" + FILE_PATH;

  @BeforeEach
  void setUp() {

    blobStore = Mockito.mock(BlobStore.class);

    featureFlagHandler = Mockito.mock(FeatureFlagHandler.class);

    localDataStore =
        new LocalDataStore(blobStore, featureFlagHandler, BUCKET_PREFIX, BUCKET_POSTFIX,
            DEFAULT_BUCKET_NAME
        );
  }

  @Test
  void whenSave_andSingleBucketIsEnabled_thenSaveToSingleBucket() throws Exception {

    BlobBuilder blobBuilderMock = mock(BlobBuilder.class);
    BlobBuilder.PayloadBlobBuilder payloadBlobBuilderMock =
        mock(BlobBuilder.PayloadBlobBuilder.class);
    Blob blobMock = mock(Blob.class);

    when(inputStream.available()).thenReturn(ZERO);
    when(payloadBlobBuilderMock.contentLength(ZERO)).thenReturn(payloadBlobBuilderMock);
    when(payloadBlobBuilderMock.contentDisposition(FILE_PATH)).thenReturn(payloadBlobBuilderMock);
    when(payloadBlobBuilderMock.build()).thenReturn(blobMock);
    when(blobBuilderMock.payload(inputStream)).thenReturn(payloadBlobBuilderMock);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(true);
    when(blobStore.blobBuilder(FILE_PATH)).thenReturn(blobBuilderMock);

    localDataStore.save(FILE_PATH, inputStream);

    verify(blobStore, times(1)).putBlob(DEFAULT_BUCKET_NAME, blobMock);
  }

  @Test
  void whenLoad_andSingleBucketIsEnabled_thenReturnFromSingleBucket() throws Exception {

    Blob mockBlob = mock(Blob.class);
    Payload mockPayload = mock(Payload.class);

    when(mockPayload.openStream()).thenReturn(inputStream);
    when(mockBlob.getPayload()).thenReturn(mockPayload);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(true);
    when(blobStore.getBlob(DEFAULT_BUCKET_NAME, FILE_PATH)).thenReturn(mockBlob);
    InputStream loaded = localDataStore.load(FILE_PATH);

    Assertions.assertEquals(inputStream, loaded);
  }

  @Test
  void whenDelete_andSingleBucketIsEnabled_thenDeleteFromSingleBucket() throws Exception {

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(true);

    localDataStore.delete(FILE_PATH);

    verify(blobStore, times(1)).removeBlob(DEFAULT_BUCKET_NAME, FILE_PATH);
  }

  @Test
  void whenSave_andSingleBucketIsDisabled_andBucketInName_thenSaveToThisBucket() throws Exception {

    BlobBuilder blobBuilderMock = mock(BlobBuilder.class);
    BlobBuilder.PayloadBlobBuilder payloadBlobBuilderMock =
        mock(BlobBuilder.PayloadBlobBuilder.class);
    Blob blobMock = mock(Blob.class);

    when(inputStream.available()).thenReturn(ZERO);
    when(payloadBlobBuilderMock.contentLength(ZERO)).thenReturn(payloadBlobBuilderMock);
    when(payloadBlobBuilderMock.contentDisposition(FILE_PATH)).thenReturn(payloadBlobBuilderMock);
    when(payloadBlobBuilderMock.build()).thenReturn(blobMock);
    when(blobBuilderMock.payload(inputStream)).thenReturn(payloadBlobBuilderMock);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(blobStore.blobBuilder(FILE_PATH)).thenReturn(blobBuilderMock);

    localDataStore.save(MULTI_FILE_PATH, inputStream);

    verify(blobStore, times(1)).putBlob(
        BUCKET_PREFIX + MULTI_BUCKET_NAME + BUCKET_POSTFIX, blobMock);
  }

  @Test
  void whenLoad_andSingleBucketIsDisabled_andBucketInName_thenReturnFromThisBucket()
      throws Exception {

    Blob mockBlob = mock(Blob.class);
    Payload mockPayload = mock(Payload.class);

    when(mockPayload.openStream()).thenReturn(inputStream);
    when(mockBlob.getPayload()).thenReturn(mockPayload);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(blobStore.getBlob(BUCKET_PREFIX + MULTI_BUCKET_NAME + BUCKET_POSTFIX,
        FILE_PATH
    )).thenReturn(mockBlob);
    InputStream loaded = localDataStore.load(MULTI_FILE_PATH);

    Assertions.assertEquals(inputStream, loaded);
  }

  @Test
  void whenDelete_andSingleBucketIsDisabled_andBucketInName_thenReturnFromThisBucket()
      throws Exception {

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    localDataStore.delete(MULTI_FILE_PATH);

    verify(blobStore, times(1)).removeBlob(
        BUCKET_PREFIX + MULTI_BUCKET_NAME + BUCKET_POSTFIX, FILE_PATH);
  }
}
