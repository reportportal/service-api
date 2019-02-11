package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class FileStorageControllerTest extends BaseMvcTest {

	@Autowired
	private DataStoreService dataStoreService;

	@Test
	public void userPhoto() throws Exception {
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/data/photo")
				.file(new MockMultipartFile("file", new ClassPathResource("image/image.png").getInputStream()))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		mockMvc.perform(get("/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		mockMvc.perform(get("/data/userphoto?id=default").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());

		mockMvc.perform(delete("/data/photo").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	public void uploadLargeUserPhoto() throws Exception {
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/data/photo")
				.file(new MockMultipartFile("file", new ClassPathResource("image/large_image.png").getInputStream()))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isBadRequest());
	}

	@Test
	public void uploadNotImage() throws Exception {
		final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/data/photo")
				.file(new MockMultipartFile("file", "text.txt", "text/plain", "test".getBytes()))
				.contentType(MediaType.MULTIPART_FORM_DATA);

		mockMvc.perform(requestBuilder.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isBadRequest());
	}

	@Test
	public void getFile() throws Exception {
		final String dataId = dataStoreService.save(2L, new ClassPathResource("image/large_image.png").getInputStream(), "large_image.png");

		mockMvc.perform(get("/data/" + dataId).with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}
}