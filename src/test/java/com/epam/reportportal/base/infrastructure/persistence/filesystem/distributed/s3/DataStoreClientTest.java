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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStoreClient;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import org.apache.opendal.Metadata;
import org.apache.opendal.Operator;
import org.apache.opendal.OperatorInputStream;
import org.apache.opendal.ReadOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    InputStream inputStream = new ByteArrayInputStream(content);

    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    String fullPath = BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME;
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    dataStoreClient.save(filePath, inputStream);

    verify(operator, times(1)).write(eq(fullPath), aryEq(content));
  }

  @Test
  @DisplayName("Load opens an operator stream under the prefixed bucket path without reading the file")
  void load() throws Exception {
    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    String fullPath = BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME;
    OperatorInputStream stream = mock(OperatorInputStream.class);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(operator.createInputStream(fullPath)).thenReturn(stream);
    when(stream.read()).thenReturn((int) 'd', -1);

    InputStream loaded = dataStoreClient.load(filePath);

    assertEquals('d', loaded.read());
    assertEquals(-1, loaded.read());
    verify(operator, never()).read(any(String.class));
  }

  @Test
  @DisplayName("Get size returns the object length from stat")
  void getSize() {
    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    String fullPath = BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/" + FILE_NAME;
    Metadata metadata = mock(Metadata.class);

    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(operator.stat(fullPath)).thenReturn(metadata);
    when(metadata.getContentLength()).thenReturn(42L);

    assertEquals(42L, dataStoreClient.getSize(filePath));
    verify(operator, never()).read(any(String.class));
  }

  @Test
  @DisplayName("Get size of a missing object fails with UNABLE_TO_LOAD_BINARY_DATA")
  void getSizeOfMissingFile() {
    String filePath = DEFAULT_BUCKET_NAME + "/" + FILE_NAME;
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(operator.stat(any(String.class))).thenThrow(new RuntimeException("not found"));

    var exception = assertThrows(ReportPortalException.class,
        () -> dataStoreClient.getSize(filePath));
    assertEquals(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, exception.getErrorType());
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

  @ParameterizedTest
  @ValueSource(strings = {"prj/../x", "../x", "/etc/passwd", "prj/2024-1/..", "prj\\..\\x",
      "\\x"})
  @DisplayName("Save, load, load range and delete reject keys escaping the storage root")
  void rejectsTraversalKeys(String filePath) {
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    assertThrows(ReportPortalException.class,
        () -> dataStoreClient.save(filePath, new ByteArrayInputStream(new byte[] {1})));
    assertThrows(ReportPortalException.class, () -> dataStoreClient.load(filePath));
    assertThrows(ReportPortalException.class, () -> dataStoreClient.loadRange(filePath, 0, 1));
    assertThrows(ReportPortalException.class, () -> dataStoreClient.delete(filePath));
    verifyNoInteractions(operator);
  }

  @Test
  @DisplayName("Keys with dots inside a segment are accepted")
  void acceptsDotsInsideSegment() {
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);

    dataStoreClient.delete(DEFAULT_BUCKET_NAME + "/file..name.png");

    verify(operator).delete(BUCKET_PREFIX + DEFAULT_BUCKET_NAME + BUCKET_POSTFIX + "/file..name.png");
  }

  @Test
  @DisplayName("Filesystem store streams the saved file, reports its size and fails on a missing file")
  void filesystemRoundTrip(@TempDir Path root) throws Exception {
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    try (Operator fsOperator = Operator.of("fs", Map.of("root", root.toString()))) {
      var client = new DataStoreClient(fsOperator, BUCKET_PREFIX, BUCKET_POSTFIX,
          DEFAULT_BUCKET_NAME, featureFlagHandler);
      byte[] content = "0123456789".getBytes();
      String filePath = "1/2026-11/launch/file.txt";

      client.save(filePath, new ByteArrayInputStream(content));

      assertEquals(content.length, client.getSize(filePath));
      try (InputStream loaded = client.load(filePath)) {
        assertArrayEquals(content, loaded.readAllBytes());
      }
      assertThrows(ReportPortalException.class, () -> client.getSize("1/2026-11/launch/missing"));
      assertThrows(ReportPortalException.class, () -> {
        try (InputStream missing = client.load("1/2026-11/launch/missing")) {
          missing.readAllBytes();
        }
      });
    }
  }
}
