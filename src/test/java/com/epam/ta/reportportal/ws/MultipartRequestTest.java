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

package com.epam.ta.reportportal.ws;

import com.epam.ta.reportportal.auth.AuthConstants;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Add new test for multipart requests
 *
 * @author Andrei Varabyeu
 */
public class MultipartRequestTest extends BaseMvcTest {

	private static final String DEMO_FILE_NAME = "picture.png";

	@Test
	public void testPageableLast() throws Exception {
		Resource multipartFile = new ClassPathResource(DEMO_FILE_NAME);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(DEMO_FILE_NAME, multipartFile.getInputStream());

		this.mvcMock.perform(fileUpload(PROJECT_BASE_URL + "/log").file(mockMultipartFile)
				.secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))).andExpect(status().is(400));

	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}