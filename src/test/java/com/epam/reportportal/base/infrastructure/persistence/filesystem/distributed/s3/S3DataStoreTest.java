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

package com.epam.reportportal.base.infrastructure.persistence.filesystem.distributed.s3;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.distributed.s3.S3DataStore;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import java.io.InputStream;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.io.Payload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class S3DataStoreTest {

  private static final String FILE_NAME = "someFile";
  private static final String BUCKET_PREFIX = "prj-";
  private static final String BUCKET_POSTFIX = "-postfix";
  private static final String DEFAULT_BUCKET_NAME = "rp-bucket";
  private static final String REGION = "us-east-1";
  private static final int ZERO = 0;

  private final BlobStore blobStore = mock(BlobStore.class);
  private final InputStream inputStream = mock(InputStream.class);

  private final FeatureFlagHandler featureFlagHandler = mock(FeatureFlagHandler.class);

  private final S3DataStore s3DataStore =
      new S3DataStore(blobStore, BUCKET_PREFIX, BUCKET_POSTFIX, DEFAULT_BUCKET_NAME, REGION,
          featureFlagHandler
      );

  @Test
  void save() throws Exception {

    BlobBuilder blobBuilderMock = mock(BlobBuilder.class);
    BlobBuilder.PayloadBlobBuilder payloadBlobBuilderMock =
        mock(BlobBuilder.PayloadBlobBuilder.class);
    Blob blobMock = mock(Blob.class);

    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;

    when(inputStream.available()).thenReturn(ZERO);
    when(payloadBlobBuilderMock.contentDisposition(FILE_NAME)).thenReturn(payloadBlobBuilderMock);
    when(payloadBlobBuilderMock.contentLength(ZERO)).thenReturn(payloadBlobBuilderMock);
    when(payloadBlobBuilderMock.build()).thenReturn(blobMock);
    when(blobBuilderMock.payload(inputStream)).thenReturn(payloadBlobBuilderMock);

    when(blobStore.containerExists(any(String.class))).thenReturn(true);
    when(blobStore.blobBuilder(FILE_NAME)).thenReturn(blobBuilderMock);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    s3DataStore.save(filePath, inputStream);

    verify(blobStore, times(1)).putBlob(
        BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX, blobMock);
  }

  @Test
  void load() throws Exception {

    Blob mockBlob = mock(Blob.class);
    Payload mockPayload = mock(Payload.class);

    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;

    when(mockPayload.openStream()).thenReturn(inputStream);
    when(mockBlob.getPayload()).thenReturn(mockPayload);

    when(blobStore.getBlob(BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX,
        FILE_NAME
    )).thenReturn(mockBlob);
    InputStream loaded = s3DataStore.load(filePath);

    Assertions.assertEquals(inputStream, loaded);
  }

  @Test
  void delete() throws Exception {

    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;

    s3DataStore.delete(filePath);

    verify(blobStore, times(1)).removeBlob(
        BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX, FILE_NAME);
  }
}
