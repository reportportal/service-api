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
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SubmittingIssueMvcTest extends BaseMvcTest {

	private static final String RESOURCE = "/item";

	@Autowired
	private TestItemRepository testItemRepository;

	@Test
	@Ignore
	public void testDesc() throws Exception {
		this.mvcMock.perform(put(PROJECT_BASE_URL + RESOURCE).secure(true)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"submit_new\": true,\"issues\": [{\"test_item_id\": \"44524cc1553de753b3e5bb2f\",\"issue\": {\"issue_type\": \"Automation bug\",\"comment\": \"test\"}},{\"test_item_id\": \"44524cc1553de753b3e5cc2f\",\"issue\": {\"issue_type\": \"Automation bug\",\"comment\": \"test\"}}]}")
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))).andExpect(status().isOk());

	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}