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

package com.epam.ta.reportportal.ws.validation;

import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.Matchers.*;

public class JaskonRequiredPropertiesValidatorTest {

	@Test
	public void testRequiredFields() {
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName("some launch name");
		startLaunchRQ.setTags(Collections.emptySet());
		JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
		Errors errors = new BeanPropertyBindingResult(startLaunchRQ, "startLaunchRq");
		validator.validate(startLaunchRQ, errors);
		Assert.assertThat(errors.getAllErrors(), not(empty()));
		Assert.assertThat(errors.getFieldError("startTime"), not(nullValue()));
	}

	@Test
	public void testInnerRequiredFields() {
		IssueDefinition issueRQ = new IssueDefinition();
		JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
		Errors errors = new BeanPropertyBindingResult(issueRQ, "issueRQ");
		validator.validate(issueRQ, errors);
		Assert.assertThat(errors.getAllErrors(), not(empty()));
		Assert.assertThat(errors.getFieldError("issue"), not(nullValue()));
	}

	@Test
	public void testInnerRequiredFields1() {
		FinishTestItemRQ issueRQ = new FinishTestItemRQ();
		issueRQ.setEndTime(Calendar.getInstance().getTime());
		issueRQ.setStatus("PASSED");
		JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
		Errors errors = new BeanPropertyBindingResult(issueRQ, "issueRQ");
		validator.validate(issueRQ, errors);
		Assert.assertThat(errors.getAllErrors(), empty());
	}

	@Test
	public void testInnerRequiredFields2() {
		FinishTestItemRQ issueRQ = new FinishTestItemRQ();
		issueRQ.setEndTime(Calendar.getInstance().getTime());
		issueRQ.setStatus("PASSED");
		issueRQ.setIssue(new Issue());
		JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
		Errors errors = new BeanPropertyBindingResult(issueRQ, "issueRQ");
		validator.validate(issueRQ, errors);
		Assert.assertThat(errors.getAllErrors(), not(empty()));
	}
}