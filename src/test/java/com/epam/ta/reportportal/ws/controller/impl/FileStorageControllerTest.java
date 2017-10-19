/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
public class FileStorageControllerTest extends BaseMvcTest {

	@Test
	public void addPhotoWidthNegative() throws Exception {
		File file = new File("src/test/resources/500x383.jpg");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload("/data/photo")
				.file(new MockMultipartFile("file", new FileInputStream(file)))
				.principal(authentication());
		this.mvcMock.perform(builder).andExpect(status().is(400));
	}

	@Test
	public void addPhotoHeightNegative() throws Exception {
		File file = new File("src/test/resources/209x505.png");
		this.mvcMock.perform(MockMvcRequestBuilders.fileUpload("/data/photo")
				.file(new MockMultipartFile("file", new FileInputStream(file)))
				.principal(authentication())).andExpect(status().is(400));
	}

	@Test
	public void addPhotoExtensionNegative() throws Exception {
		File file = new File("src/test/resources/logback.xml");
		this.mvcMock.perform(MockMvcRequestBuilders.fileUpload("/data/photo")
				.file(new MockMultipartFile("file", new FileInputStream(file)))
				.principal(authentication())).andExpect(status().is(400));
	}

	@Test
	public void addPhotoSizeNegative() throws Exception {
		File file = new File("src/test/resources/locators.pdf");
		this.mvcMock.perform(MockMvcRequestBuilders.fileUpload("/data/photo")
				.file(new MockMultipartFile("file", new FileInputStream(file)))
				.principal(authentication())).andExpect(status().is(400));
	}

	@Test
	public void addPhotoPositive() throws Exception {
		File file = new File("src/test/resources/picture.png");
		this.mvcMock.perform(MockMvcRequestBuilders.fileUpload("/data/photo")
				.file(new MockMultipartFile("file", new FileInputStream(file)))
				.principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void deleteUserPhoto() throws Exception {
		this.mvcMock.perform(delete("/data/photo").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void deleteUserPhotoUpsaUser() throws Exception {
		this.mvcMock.perform(delete("/data/photo").principal(AuthConstants.UPSA_USER)).andExpect(status().isForbidden());
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}