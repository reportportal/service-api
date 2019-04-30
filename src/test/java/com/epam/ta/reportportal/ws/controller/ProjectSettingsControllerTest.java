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

import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.UpdateOneIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import com.epam.ta.reportportal.ws.model.project.config.pattern.UpdatePatternTemplateRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/project-settings/project-settings-fill.sql")
class ProjectSettingsControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private IssueTypeRepository issueTypeRepository;

	@Test
	void createSubType() throws Exception {
		CreateIssueSubTypeRQ rq = new CreateIssueSubTypeRQ();
		rq.setTypeRef("PRODUCT_BUG");
		rq.setColor("color");
		rq.setLongName("LongName");
		rq.setShortName("name");
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/settings/sub-type").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated());
	}

	@Test
	void getProjectSettings() throws Exception {
		final MvcResult mvcResult = mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/settings").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk())
				.andReturn();
		final ProjectSettingsResource projectSettingsResource = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				ProjectSettingsResource.class
		);
		assertEquals(8,
				projectSettingsResource.getSubTypes().values().stream().flatMap(Collection::stream).collect(Collectors.toList()).size()
		);
	}

	@Test
	void deleteSubType() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/settings/sub-type/6").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());

		Optional<IssueType> byId = issueTypeRepository.findById(6L);
		assertFalse(byId.isPresent());

	}

	@Test
	void updateSubType() throws Exception {
		UpdateIssueSubTypeRQ request = new UpdateIssueSubTypeRQ();
		final UpdateOneIssueSubTypeRQ updateOneIssueSubTypeRQ = new UpdateOneIssueSubTypeRQ();
		updateOneIssueSubTypeRQ.setColor("updated");
		updateOneIssueSubTypeRQ.setLocator("custom_ti");
		updateOneIssueSubTypeRQ.setLongName("updated");
		updateOneIssueSubTypeRQ.setShortName("upd");
		updateOneIssueSubTypeRQ.setTypeRef("TO_INVESTIGATE");
		request.setIds(Collections.singletonList(updateOneIssueSubTypeRQ));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/settings/sub-type").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void createPatternTemplate() throws Exception {
		CreatePatternTemplateRQ createPatternTemplateRQ = new CreatePatternTemplateRQ();
		createPatternTemplateRQ.setEnabled(true);
		createPatternTemplateRQ.setName("another_name");
		createPatternTemplateRQ.setType("string");
		createPatternTemplateRQ.setValue("qwe");
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/settings/pattern").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(createPatternTemplateRQ))).andExpect(status().isCreated());
	}

	@Test
	void createPatternTemplateWithWrongType() throws Exception {
		CreatePatternTemplateRQ createPatternTemplateRQ = new CreatePatternTemplateRQ();
		createPatternTemplateRQ.setEnabled(true);
		createPatternTemplateRQ.setName("name");
		createPatternTemplateRQ.setType("dd");
		createPatternTemplateRQ.setValue("qwe");
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/settings/pattern").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(createPatternTemplateRQ))).andExpect(status().isBadRequest());
	}

	@Test
	void createPatternTemplateWithDuplicateName() throws Exception {
		CreatePatternTemplateRQ createPatternTemplateRQ = new CreatePatternTemplateRQ();
		createPatternTemplateRQ.setEnabled(true);
		createPatternTemplateRQ.setName("some_name");
		createPatternTemplateRQ.setType("string");
		createPatternTemplateRQ.setValue("qwe");

		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/settings/pattern").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(createPatternTemplateRQ))).andExpect(status().isConflict());
	}

	@Test
	void updatePatternTemplate() throws Exception {

		UpdatePatternTemplateRQ updatePatternTemplateRQ = new UpdatePatternTemplateRQ();
		updatePatternTemplateRQ.setName("another_name");
		updatePatternTemplateRQ.setEnabled(true);

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/settings/pattern/1").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(updatePatternTemplateRQ))).andExpect(status().isOk());
	}

	@Test
	void updatePatternTemplateWithTheSameName() throws Exception {

		UpdatePatternTemplateRQ updatePatternTemplateRQ = new UpdatePatternTemplateRQ();
		updatePatternTemplateRQ.setName("some_name");
		updatePatternTemplateRQ.setEnabled(true);

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/settings/pattern/1").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(updatePatternTemplateRQ))).andExpect(status().isOk());
	}

	@Test
	void updatePatternTemplateWithDuplicateName() throws Exception {

		UpdatePatternTemplateRQ updatePatternTemplateRQ = new UpdatePatternTemplateRQ();
		updatePatternTemplateRQ.setName("some_name");
		updatePatternTemplateRQ.setEnabled(true);

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/settings/pattern/2").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(updatePatternTemplateRQ))).andExpect(status().isConflict());
	}
}