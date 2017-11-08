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

import com.dumbster.smtp.ServerOptions;
import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;
import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Administration controller test
 *
 * @author Dzmitry_Kavalets
 */
public class SettingsControllerTest extends BaseMvcTest {

	private static SmtpServer SMTP;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeClass
	public static void startSmtpServer() throws IOException {
		ServerOptions so = new ServerOptions();
		so.port = 10025;
		SMTP = SmtpServerFactory.startServer(so);
	}

	@AfterClass
	public static void shutdownSmtpServer() {
		SMTP.stop();
	}

	@Test
	public void getServerSettings() throws Exception {
		this.mvcMock.perform(get("/settings/default").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void updateServerSettingsNegative() throws Exception {
		ServerEmailResource rq = new ServerEmailResource();
		rq.setHost("fake.host.com");
		rq.setPort(25);
		rq.setProtocol("smtp");
		this.mvcMock.perform(put("/settings/default/email").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(400));
	}

	@Test
	public void updateServerSettings() throws Exception {
		ServerEmailResource rq = new ServerEmailResource();
		rq.setHost("localhost");
		rq.setPort(10025);
		rq.setProtocol("smtp");
		rq.setAuthEnabled(true);
		rq.setUsername("user");
		rq.setPassword("password");
		this.mvcMock.perform(put("/settings/default/email").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void deleteServerSettings() throws Exception {
		updateServerSettings();
		this.mvcMock.perform(delete("/settings/default/email").principal(authentication()).contentType(APPLICATION_JSON))
				.andExpect(status().is(200));
	}

	@Test
	public void saveAnalyticsSettingsNegative() throws Exception {
		AnalyticsResource resource = new AnalyticsResource();
		resource.setEnabled(true);
		resource.setType("");
		this.mvcMock.perform(put("/settings/default/analytics").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resource))).andExpect(status().is(400));
	}

	@Test
	public void saveAnalyticsSettings() throws Exception {
		AnalyticsResource resource = new AnalyticsResource();
		resource.setEnabled(true);
		resource.setType("google");
		this.mvcMock.perform(put("/settings/default/analytics").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resource))).andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}