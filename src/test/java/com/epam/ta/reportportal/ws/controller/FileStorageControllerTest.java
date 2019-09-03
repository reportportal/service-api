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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;
import java.util.Optional;

import static com.epam.ta.reportportal.util.MultipartFileUtils.getMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class FileStorageControllerTest extends BaseMvcTest {

	@Autowired
	private DataStoreService dataStoreService;

	@Test
	void userPhoto() throws Exception {
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/v1/data/photo")
				.file(new MockMultipartFile("file", new ClassPathResource("image/image.png").getInputStream()))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		mockMvc.perform(get("/v1/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		mockMvc.perform(get("/v1/data/userphoto?id=default").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());

		mockMvc.perform(delete("/v1/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
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
				.file(new MockMultipartFile("file", "text.txt", "text/plain", "test".getBytes(Charset.forName("UTF-8"))))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void getFile() throws Exception {
		Optional<BinaryDataMetaInfo> binaryDataMetaInfo = dataStoreService.saveLog(2L, getMultipartFile("image/large_image.png"));

		mockMvc.perform(get("/v1/data/" + binaryDataMetaInfo.get().getFileId()).with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/data/" + binaryDataMetaInfo.get().getThumbnailFileId()).with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getUserPhotoNegative() throws Exception {
		mockMvc.perform(get("/v1/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void getUserPhotoByLoginNegative() throws Exception {
		mockMvc.perform(get("/v1/data/userphoto?id=default").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getNotExistUserPhoto() throws Exception {
		mockMvc.perform(get("/v1/data/userphoto?id=not_exist").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isNotFound());
	}
}