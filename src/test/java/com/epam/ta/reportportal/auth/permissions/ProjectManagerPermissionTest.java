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

package com.epam.ta.reportportal.auth.permissions;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Project controller permissions test
 *
 * @author Andrei Varabyeu
 */
public class ProjectManagerPermissionTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Request to non existing project
	 *
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	@Test
	public void projectNotFound() throws Exception {
		this.mvcMock.perform(put("/project/notexisting").principal(AuthConstants.PROJECT_USER)
				.content(objectMapper.writeValueAsBytes(getUpdateRequest()))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	/**
	 * Request from user which is not assigned to specified project
	 *
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	@Test
	public void userNotAssigned() throws Exception {
		Authentication vasia = AuthConstants.newAuthentication("Vasia", AuthConstants.USER_PASSWORD, true,
				new SimpleGrantedAuthority(UserRole.USER.name())
		);
		SecurityContextHolder.getContext().setAuthentication(vasia);
		this.mvcMock.perform(put("/project" + PROJECT_BASE_URL).principal(vasia)
				.content(objectMapper.writeValueAsBytes(getUpdateRequest()))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
	}

	private UpdateProjectRQ getUpdateRequest() {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		rq.setCustomer("some customer");
		return rq;
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.PROJECT_USER;
	}

	public static class MockSecurityContext implements SecurityContext {

		private static final long serialVersionUID = -1386535243513362694L;

		private Authentication authentication;

		public MockSecurityContext(Authentication authentication) {
			this.authentication = authentication;
		}

		@Override
		public Authentication getAuthentication() {
			return this.authentication;
		}

		@Override
		public void setAuthentication(Authentication authentication) {
			this.authentication = authentication;
		}
	}
}