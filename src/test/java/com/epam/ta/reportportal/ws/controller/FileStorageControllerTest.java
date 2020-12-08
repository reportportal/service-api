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

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.epam.ta.reportportal.util.MultipartFileUtils.getMultipartFile;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/v1/data/photo")
				.file(new MockMultipartFile("file", "file", "image/png", new ClassPathResource("image/image.png").getInputStream()))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		mockMvc.perform(get("/v1/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		mockMvc.perform(get("/v1/data/default_personal/userphoto?id=default").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/v1/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	@Sql("/db/user/user-customer.sql")
	public void testUserPhotoAccessDeniedForCustomer() throws Exception {
		mockMvc.perform(get("/v1/data/default_personal/userphoto?id=default").with(token(oAuthHelper.getCustomerToken())))
				.andExpect(status().isForbidden());
	}

	@Test
	void uploadLargeUserPhoto() throws Exception {
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/v1/data/photo")
				.file(new MockMultipartFile("file", new ClassPathResource("image/large_image.png").getInputStream()))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void uploadNotImage() throws Exception {
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/v1/data/photo")
				.file(new MockMultipartFile("file", "text.txt", "text/plain", "test".getBytes(StandardCharsets.UTF_8)))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isBadRequest());
	}

	@Test
	@Sql("/db/data-store/data-store-fill.sql")
	void getFile() throws Exception {
		AttachmentMetaInfo metaInfo = AttachmentMetaInfo.builder()
				.withProjectId(1L)
				.withItemId(1L)
				.withLaunchId(1L)
				.withLogId(1L)
				.withLogUuid("uuid")
				.withLaunchUuid("uuid")
				.build();
		Optional<BinaryDataMetaInfo> binaryDataMetaInfo = attachmentBinaryDataService.saveAttachment(metaInfo,
				getMultipartFile("image/large_image.png")
		);
		assertTrue(binaryDataMetaInfo.isPresent());
		attachmentBinaryDataService.attachToLog(binaryDataMetaInfo.get(), metaInfo);

		Optional<Attachment> attachment = attachmentRepository.findByFileId(binaryDataMetaInfo.get().getFileId());

		assertTrue(attachment.isPresent());

		mockMvc.perform(get("/v1/data/superadmin_personal/" + attachment.get().getId()).with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getUserPhotoNegative() throws Exception {
		mockMvc.perform(get("/v1/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getUserPhotoByLoginNegative() throws Exception {
		mockMvc.perform(get("/v1/data/superadmin_personal/userphoto?id=superadmin").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getNotExistUserPhoto() throws Exception {
		mockMvc.perform(get("/v1/data/userphoto?id=not_exist").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isNotFound());
	}
}