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

package com.epam.reportportal.base.infrastructure.persistence.filesystem.distributed.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStoreClient;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import java.io.InputStream;
import org.apache.opendal.Operator;
import org.apache.opendal.OperatorInputStream;
import org.apache.opendal.ReadOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class DataStoreClientTest {

  private static final String FILE_NAME = "someFile";
  private static final String BUCKET_PREFIX = "prj-";
  private static final String BUCKET_POSTFIX = "-postfix";
  private static final String DEFAULT_BUCKET_NAME = "rp-bucket";

  private final Operator operator = mock(Operator.class);

  private final FeatureFlagHandler featureFlagHandler = mock(FeatureFlagHandler.class);

  private final DataStoreClient dataStoreClient =
      new DataStoreClient(operator, BUCKET_PREFIX, BUCKET_POSTFIX, DEFAULT_BUCKET_NAME, featureFlagHandler);

  @Test
  @DisplayName("Save writes the file bytes to the operator under the prefixed bucket path")
  void save() throws Exception {
    byte[] content = "data".getBytes();
    InputStream inputStream = mock(InputStream.class);
    when(inputStream.readAllBytes()).thenReturn(content);

    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    dataStoreClient.save(filePath, inputStream);

    verify(operator, times(1))
        .write(BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME, content);
  }

  @Test
  @DisplayName("Load reads the file bytes from the operator under the prefixed bucket path")
  void load() throws Exception {
    byte[] content = "data".getBytes();
    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(operator.read(BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME))
        .thenReturn(content);

    InputStream loaded = dataStoreClient.load(filePath);

    Assertions.assertArrayEquals(content, loaded.readAllBytes());
  }

  @Test
  @DisplayName("Load range opens an operator stream with bounded read options")
  void loadRange() {
    long offset = 100;
    long length = 200;
    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    String fullPath = BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME;
    OperatorInputStream rangeStream = mock(OperatorInputStream.class);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(operator.createInputStream(eq(fullPath), any(ReadOptions.class)))
        .thenReturn(rangeStream);

    InputStream loaded = dataStoreClient.loadRange(filePath, offset, length);

    assertSame(rangeStream, loaded);
    var optionsCaptor = ArgumentCaptor.forClass(ReadOptions.class);
    verify(operator).createInputStream(eq(fullPath), optionsCaptor.capture());
    assertEquals(offset, optionsCaptor.getValue().offset);
    assertEquals(length, optionsCaptor.getValue().length);
  }

  @Test
  @DisplayName("Delete removes the file from the operator under the prefixed bucket path")
  void delete() throws Exception {
    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    dataStoreClient.delete(filePath);

    verify(operator, times(1)).delete(
        BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME);
  }
}
