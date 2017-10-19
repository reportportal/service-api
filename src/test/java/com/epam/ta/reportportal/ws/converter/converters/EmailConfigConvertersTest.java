/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class EmailConfigConvertersTest {

	@Test
	public void testConvertToResource() {
		ProjectEmailConfig config = new ProjectEmailConfig();
		EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase("sendCase");
		emailSenderCase.setTags(ImmutableList.<String>builder().add("tag").build());
		emailSenderCase.setRecipients(ImmutableList.<String>builder().add("recipient").build());
		emailSenderCase.setLaunchNames(ImmutableList.<String>builder().add("launch").build());
		config.setEmailCases(ImmutableList.<EmailSenderCase>builder().add(emailSenderCase).build());
		config.setFrom("from");
		config.setEmailEnabled(true);
		ProjectEmailConfigDTO resource = EmailConfigConverters.TO_RESOURCE.apply(config);
		validate(config, resource);
	}

	@Test
	public void testConvertToModel() {

	}

	private void validate(ProjectEmailConfig config, ProjectEmailConfigDTO resource) {
		Assert.assertEquals(config.getEmailEnabled(), resource.getEmailEnabled());
		Assert.assertEquals(config.getFrom(), resource.getFrom());
		EmailSenderCase senderCase = config.getEmailCases().get(0);
		EmailSenderCaseDTO caseDTO = resource.getEmailCases().get(0);
		Assert.assertEquals(senderCase.getLaunchNames(), caseDTO.getLaunchNames());
		Assert.assertEquals(senderCase.getRecipients(), caseDTO.getRecipients());
		Assert.assertEquals(senderCase.getSendCase(), caseDTO.getSendCase());
		Assert.assertEquals(senderCase.getTags(), caseDTO.getTags());
	}

}