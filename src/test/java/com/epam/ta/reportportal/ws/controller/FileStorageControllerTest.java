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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.util.MultipartFileUtils.getMultipartFile;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class FileStorageControllerTest extends BaseMvcTest {

  @Autowired
  private AttachmentBinaryDataService attachmentBinaryDataService;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Test
  void userPhoto() throws Exception {
    final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
            "/users/2/avatar")
        .file(new MockMultipartFile("file", "file", "image/png",
            new ClassPathResource("image/image.png").getInputStream()))
        .contentType(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/users/2/avatar").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/users/2/avatar").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNoContent());
  }

  @Test
  void uploadLargeUserPhoto() throws Exception {
    final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
            "/users/2/avatar")
        .file(new MockMultipartFile("file",
            new ClassPathResource("image/large_image.png").getInputStream()))
        .contentType(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void cleanAttachmentsByCvsForbidden() throws Exception {
    final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
            "/v1/data/clean")
        .file(new MockMultipartFile("file",
            new ClassPathResource("attachments.csv").getInputStream()))
        .contentType(MediaType.MULTIPART_FORM_DATA);
    mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void cleanAttachmentsByCvs() throws Exception {
    final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
            "/v1/data/clean")
        .file(new MockMultipartFile("file",
            new ClassPathResource("attachments.csv").getInputStream()))
        .contentType(MediaType.MULTIPART_FORM_DATA);
    mockMvc.perform(requestBuilder.with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void uploadNotImage() throws Exception {
    final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(
            "/users/2/avatar")
        .file(new MockMultipartFile("file", "text.txt", "text/plain",
            "test".getBytes(StandardCharsets.UTF_8)))
        .contentType(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Sql("/db/data-store/data-store-fill.sql")
  void getFile() throws Exception {
    AttachmentMetaInfo metaInfo = AttachmentMetaInfo.builder()
        .withProjectId(1L)
        .withCreationDate(Instant.now())
        .withItemId(1L)
        .withLaunchId(1L)
        .withLogId(1L)
        .withLogUuid("uuid")
        .withLaunchUuid("uuid")
        .withFileName("name")
        .build();
    Optional<BinaryDataMetaInfo> binaryDataMetaInfo = attachmentBinaryDataService.saveAttachment(
        metaInfo,
        getMultipartFile("image/large_image.png")
    );
    assertTrue(binaryDataMetaInfo.isPresent());
    attachmentBinaryDataService.attachToLog(binaryDataMetaInfo.get(), metaInfo);

    Optional<Attachment> attachment = attachmentRepository.findByFileId(
        binaryDataMetaInfo.get().getFileId());

    assertTrue(attachment.isPresent());

    mockMvc.perform(get("/v1/data/superadmin_personal/" + attachment.get().getId()).with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }


  @Test
  void getNotExistUserPhoto() throws Exception {
    mockMvc.perform(
            get("/users/999/avatar")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }
}
