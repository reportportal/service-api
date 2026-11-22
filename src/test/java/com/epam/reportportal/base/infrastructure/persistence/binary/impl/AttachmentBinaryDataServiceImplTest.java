/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.binary.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.base.infrastructure.persistence.binary.CreateLogAttachmentService;
import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.commons.BinaryDataMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.AttachmentMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.AttachmentStreamMetadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.FilePathGenerator;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AttachmentBinaryDataServiceImplTest {

  @Mock
  private ContentTypeResolver contentTypeResolver;

  @Mock
  private FilePathGenerator filePathGenerator;

  @Mock
  private DataStoreService dataStoreService;

  @Mock
  private AttachmentRepository attachmentRepository;

  @Mock
  private CreateLogAttachmentService createLogAttachmentService;

  @Mock
  private FeatureFlagHandler featureFlagHandler;

  @Mock
  private MultipartFile file;

  private AttachmentBinaryDataServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new AttachmentBinaryDataServiceImpl(contentTypeResolver, filePathGenerator,
        dataStoreService, attachmentRepository, createLogAttachmentService, featureFlagHandler);
  }

  @Test
  void saveAttachmentReopensMultipartStreamAfterContentTypeDetection() throws Exception {
    byte[] content = "attachment content".getBytes();
    InputStream detectionStream = new ByteArrayInputStream(content);
    InputStream storageStream = new ByteArrayInputStream(content);
    AttachmentMetaInfo metaInfo = AttachmentMetaInfo.builder()
        .withProjectId(1L)
        .withLogUuid("log-uuid")
        .build();

    when(file.getContentType()).thenReturn(null);
    when(file.getInputStream()).thenReturn(detectionStream, storageStream);
    when(file.getSize()).thenReturn((long) content.length);
    when(contentTypeResolver.detectContentType(same(detectionStream))).thenReturn("image/png");
    when(filePathGenerator.generate(metaInfo)).thenReturn("generated/path");
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(dataStoreService.save(matches("generated/path/log-uuid-[0-9a-f-]{36}\\.png"),
        same(storageStream))).thenReturn("file-id");

    Optional<BinaryDataMetaInfo> result = service.saveAttachment(metaInfo, file);

    assertTrue(result.isPresent());
    assertEquals("file-id", result.orElseThrow().getFileId());
    assertEquals("image/png", result.orElseThrow().getContentType());
    assertEquals(content.length, result.orElseThrow().getFileSize());
    verify(file, times(2)).getInputStream();
    verify(dataStoreService).save(any(), same(storageStream));
  }

  @Test
  void streamingMetadataFallsBackToStoredObjectSizeWhenFileSizeIsMissing() {
    Attachment attachment = attachment(0L);
    when(attachmentRepository.findById(7L)).thenReturn(Optional.of(attachment));
    when(dataStoreService.getSize("file-id")).thenReturn(10L);

    AttachmentStreamMetadata metadata = service.getMetadataForStreaming(7L, membership());

    assertEquals(10L, metadata.fileSize());
  }

  @Test
  void streamingMetadataUsesStoredFileSizeWithoutStorageRequest() {
    Attachment attachment = attachment(25L);
    when(attachmentRepository.findById(7L)).thenReturn(Optional.of(attachment));

    AttachmentStreamMetadata metadata = service.getMetadataForStreaming(7L, membership());

    assertEquals(25L, metadata.fileSize());
    verifyNoInteractions(dataStoreService);
  }

  @Test
  void loadReturnsStorageStreamWithResolvedLength() {
    Attachment attachment = attachment(0L);
    InputStream stream = new ByteArrayInputStream(new byte[0]);
    when(attachmentRepository.findById(7L)).thenReturn(Optional.of(attachment));
    when(dataStoreService.getSize("file-id")).thenReturn(10L);
    when(dataStoreService.load("file-id")).thenReturn(Optional.of(stream));

    BinaryData binaryData = service.load(7L, membership());

    assertSame(stream, binaryData.getInputStream());
    assertEquals(10L, binaryData.getLength());
  }

  private static Attachment attachment(long fileSize) {
    Attachment attachment = new Attachment();
    attachment.setId(7L);
    attachment.setFileId("file-id");
    attachment.setFileName("file.png");
    attachment.setContentType("image/png");
    attachment.setProjectId(1L);
    attachment.setFileSize(fileSize);
    return attachment;
  }

  private static MembershipDetails membership() {
    return MembershipDetails.builder().withProjectId(1L).withProjectName("project").build();
  }

  @Test
  void saveAttachmentIgnoresPartNameAndSanitisesLogUuidInStorageKey() throws Exception {
    byte[] content = "attachment content".getBytes();
    InputStream storageStream = new ByteArrayInputStream(content);
    AttachmentMetaInfo metaInfo = AttachmentMetaInfo.builder()
        .withProjectId(1L)
        .withLogUuid("../../../tmp/evil")
        .build();

    when(file.getContentType()).thenReturn("image/png");
    when(file.getInputStream()).thenReturn(storageStream);
    lenient().when(file.getName()).thenReturn("a/../../../../tmp/evil");
    when(file.getSize()).thenReturn((long) content.length);
    when(filePathGenerator.generate(metaInfo)).thenReturn("generated/path");
    when(featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)).thenReturn(false);
    when(dataStoreService.save(any(), same(storageStream))).thenReturn("file-id");

    service.saveAttachment(metaInfo, file);

    var pathCaptor = ArgumentCaptor.forClass(String.class);
    verify(dataStoreService).save(pathCaptor.capture(), same(storageStream));
    assertTrue(pathCaptor.getValue()
            .matches("generated/path/_________tmp_evil-[0-9a-f-]{36}\\.png"),
        pathCaptor.getValue());
  }
}
