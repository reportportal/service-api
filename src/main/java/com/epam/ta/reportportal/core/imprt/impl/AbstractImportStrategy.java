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
package com.epam.ta.reportportal.core.imprt.impl;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public abstract class AbstractImportStrategy implements ImportStrategy {

  public static final String LAUNCH_NAME = "launchName";
  public static final String LAUNCH_DESCRIPTION = "description";
  public static final String ATTRIBUTE_KEY = "attributeKey";
  public static final String ATTRIBUTE_VALUE = "attributeValue";
  public static final String SKIPPED_IS_NOT_ISSUE = "skippedIsNotIssue";
  public static final String SKIPPED_ISSUE = "skippedIssue";
  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractImportStrategy.class);
  private static final Date initialStartTime = new Date(0);
  protected static final ExecutorService service = Executors.newFixedThreadPool(5);
  public static final String LAUNCH_NAME_RESTRICTION_MSG =
      "User can't import launch with the invalid number of symbols for Name.";
  public static final String LAUNCH_DESCRIPTION_RESTRICTION_MSG =
      "User can't import launch with the invalid number of symbols for Description.";
  public static final String ATTRIBUTE_KEY_RESTRICTION_MSG =
      "User can't import launch with the invalid number of symbols for Attribute Key.";
  public static final String ATTRIBUTE_KEY_WITHOUT_VALUE_MSG =
      "User can't import launch with only Attribute Key without Attribute Value.";
  public static final String ATTRIBUTE_VALUE_RESTRICTION_MSG =
      "User can't import launch with the invalid number of symbols for Attribute Value.";
  public static final String INCORRECT_NOT_ISSUE_PARAMETER_MSG =
      "User can't import launch with invalid value for parameter for NotIssue.";
  public static final int MAX_ATTRIBUTE_LENGTH = 512;
  public static final int MAX_DESCRIPTION_LENGTH = 2048;
  public static final int MAX_NAME_LENGTH = 256;

  private StartLaunchHandler startLaunchHandler;

  private FinishLaunchHandler finishLaunchHandler;

  private LaunchRepository launchRepository;

  @Autowired
  public void setStartLaunchHandler(StartLaunchHandler startLaunchHandler) {
    this.startLaunchHandler = startLaunchHandler;
  }

  @Autowired
  public void setFinishLaunchHandler(FinishLaunchHandler finishLaunchHandler) {
    this.finishLaunchHandler = finishLaunchHandler;
  }

  @Autowired
  public void setLaunchRepository(LaunchRepository launchRepository) {
    this.launchRepository = launchRepository;
  }

  protected ParseResults processResults(CompletableFuture... futures) {
    ParseResults results = new ParseResults();
    Arrays.stream(futures).map(it -> (ParseResults) it.join()).forEach(res -> {
      results.checkAndSetStartLaunchTime(res.getStartTime());
      results.increaseDuration(res.getDuration());
    });
    return results;
  }

  protected String startLaunch(ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, String launchName, Map<String, String> params) {
    StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
    startLaunchRQ.setStartTime(initialStartTime);
    startLaunchRQ.setName(params.get(LAUNCH_NAME) != null ? params.get(LAUNCH_NAME) : launchName);
    startLaunchRQ.setDescription(params.get(LAUNCH_DESCRIPTION));
    startLaunchRQ.setMode(Mode.DEFAULT);
    Set<ItemAttributesRQ> itemAttributes = getItemAttributes(params);
    startLaunchRQ.setAttributes(itemAttributes);
    return startLaunchHandler.startLaunch(user, projectDetails, startLaunchRQ).getId();
  }

  private Set<ItemAttributesRQ> getItemAttributes(Map<String, String> params) {
    Set<ItemAttributesRQ> itemAttributes = new HashSet<>();
    if (params.get(ATTRIBUTE_VALUE) != null) {
      itemAttributes.add(
          new ItemAttributesRQ(params.get(ATTRIBUTE_KEY), params.get(ATTRIBUTE_VALUE)));
    }
    if (params.get(SKIPPED_IS_NOT_ISSUE) != null && Boolean.parseBoolean(params.get(
        SKIPPED_IS_NOT_ISSUE))) {
      itemAttributes.add(new ItemAttributesRQ(SKIPPED_ISSUE, "true", true));
    }
    return itemAttributes;
  }

  protected void finishLaunch(String launchId, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user,
      ParseResults results, String baseUrl) {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(results.getEndTime());
    finishLaunchHandler.finishLaunch(launchId, finishExecutionRQ, projectDetails, user, baseUrl);
    Launch launch = launchRepository.findByUuid(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
    launch.setStartTime(results.getStartTime());
    launchRepository.save(launch);
  }

  /**
   * Got a cause exception message if it has any.
   *
   * @param e Exception
   * @return Clean exception message
   */
  protected String cleanMessage(Exception e) {
    if (e.getCause() != null) {
      return e.getCause().getMessage();
    }
    return e.getMessage();
  }

  /*
   * if the importing results do not contain initial timestamp a launch gets
   * a default date if the launch is broken, time should be updated to not to broke
   * the statistics
   */
  protected void updateBrokenLaunch(String savedLaunchId) {
    if (savedLaunchId != null) {
      Launch launch = launchRepository.findByUuid(savedLaunchId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND));
      launch.setStartTime(LocalDateTime.now());
      launch.setStatus(StatusEnum.INTERRUPTED);
      launchRepository.save(launch);
    }
  }

  protected void validateOverrideParameters(Map<String, String> params) {
    validateLaunchName(params);
    validateLaunchDescription(params);
    validateAttributeKey(params);
    validateAttributeKeyWithValue(params);
    validateAttributeValue(params);
    validateSkippedParameter(params);
  }

  private void validateLaunchName(Map<String, String> params) {
    String launchName = params.get(LAUNCH_NAME);
    boolean isValid = launchName == null || (1 < launchName.length() && launchName.length() <= MAX_NAME_LENGTH);
    expect(isValid, Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, LAUNCH_NAME_RESTRICTION_MSG);
  }

  private void validateLaunchDescription(Map<String, String> params) {
    String launchDescription = params.get(LAUNCH_DESCRIPTION);
    boolean isValid = launchDescription == null || (launchDescription.length() <= MAX_DESCRIPTION_LENGTH);
    expect(isValid, Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, LAUNCH_DESCRIPTION_RESTRICTION_MSG);
  }

  private void validateAttributeKey(Map<String, String> params) {
    String attributeKey = params.get(ATTRIBUTE_KEY);
    boolean isValid = attributeKey == null || (attributeKey.length() <= MAX_ATTRIBUTE_LENGTH);
    expect(isValid, Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTE_KEY_RESTRICTION_MSG);
  }

  private void validateAttributeKeyWithValue(Map<String, String> params) {
    String attributeKey = params.get(ATTRIBUTE_KEY);
    String attributeValue = params.get(ATTRIBUTE_VALUE);
    boolean isValid = attributeKey == null || attributeValue != null;
    expect(isValid, Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTE_KEY_WITHOUT_VALUE_MSG);
  }

  private void validateAttributeValue(Map<String, String> params) {
    String attributeValue = params.get(ATTRIBUTE_VALUE);
    boolean isValid = attributeValue == null || (attributeValue.length() <= MAX_ATTRIBUTE_LENGTH);
    expect(isValid, Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTE_VALUE_RESTRICTION_MSG);
  }

  private void validateSkippedParameter(Map<String, String> params) {
    String notIssue = params.get(SKIPPED_IS_NOT_ISSUE);
    boolean isValid =
        notIssue == null || "true".equalsIgnoreCase(notIssue) || "false".equalsIgnoreCase(notIssue);
    expect(isValid, Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR,
        INCORRECT_NOT_ISSUE_PARAMETER_MSG);
  }
}
