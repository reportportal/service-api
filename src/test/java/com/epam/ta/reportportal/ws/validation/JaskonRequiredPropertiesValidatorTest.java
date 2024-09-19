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

package com.epam.ta.reportportal.ws.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.epam.ta.reportportal.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.Issue;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

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
    assertThat(errors.getAllErrors(), not(empty()));
    assertThat(errors.getFieldError("startTime"), not(nullValue()));
  }

  @Test
  public void testInnerRequiredFields() {
    IssueDefinition issueRQ = new IssueDefinition();
    JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
    Errors errors = new BeanPropertyBindingResult(issueRQ, "issueRQ");
    validator.validate(issueRQ, errors);
    assertThat(errors.getAllErrors(), not(empty()));
    assertThat(errors.getFieldError("issue"), not(nullValue()));
  }

  @Test
  public void testInnerRequiredFields1() {
    FinishTestItemRQ issueRQ = new FinishTestItemRQ();
    issueRQ.setLaunchUuid(UUID.randomUUID().toString());
    issueRQ.setEndTime(Instant.now());
    issueRQ.setStatus("PASSED");
    JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
    Errors errors = new BeanPropertyBindingResult(issueRQ, "issueRQ");
    validator.validate(issueRQ, errors);
    assertThat(errors.getAllErrors(), empty());
  }

  @Test
  public void testInnerRequiredFields2() {
    FinishTestItemRQ issueRQ = new FinishTestItemRQ();
    issueRQ.setEndTime(Instant.now());
    issueRQ.setStatus("PASSED");
    issueRQ.setIssue(new Issue());
    JaskonRequiredPropertiesValidator validator = new JaskonRequiredPropertiesValidator();
    Errors errors = new BeanPropertyBindingResult(issueRQ, "issueRQ");
    validator.validate(issueRQ, errors);
    assertThat(errors.getAllErrors(), not(empty()));
  }

}
