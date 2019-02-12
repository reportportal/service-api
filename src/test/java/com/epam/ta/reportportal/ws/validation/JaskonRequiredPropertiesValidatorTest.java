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

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class JaskonRequiredPropertiesValidatorTest {

	@Test
	public void testRequiredFields() {
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName("some launch name");
		startLaunchRQ.setAttributes(Collections.emptySet());
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