/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.events.handler.launch;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.extractStatisticsCount;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_AUTOMATION_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_PRODUCT_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_SYSTEM_ISSUE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_TO_INVESTIGATE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_TOTAL;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.reportportal.rules.exception.ErrorType;
import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchNotificationRunner
    implements ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchNotificationRunner.class);

  private static final String EMAIL_INTEGRATION_NAME = "email server";

  private final GetProjectHandler getProjectHandler;
  private final GetLaunchHandler getLaunchHandler;
  private final GetIntegrationHandler getIntegrationHandler;
  private final MailServiceFactory mailServiceFactory;
  private final UserRepository userRepository;

  @Autowired
  public LaunchNotificationRunner(GetProjectHandler getProjectHandler,
      GetLaunchHandler getLaunchHandler, GetIntegrationHandler getIntegrationHandler,
      MailServiceFactory mailServiceFactory, UserRepository userRepository) {
    this.getProjectHandler = getProjectHandler;
    this.getLaunchHandler = getLaunchHandler;
    this.getIntegrationHandler = getIntegrationHandler;
    this.mailServiceFactory = mailServiceFactory;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public void handle(LaunchFinishedEvent launchFinishedEvent, Map<String, String> projectConfig) {

    boolean isNotificationsEnabled = BooleanUtils.toBoolean(
        projectConfig.get(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute()))
        && BooleanUtils.toBoolean(
        projectConfig.get(ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED.getAttribute()));

    if (isNotificationsEnabled) {
      getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(
              launchFinishedEvent.getProjectId(), IntegrationGroupEnum.NOTIFICATION)
          .filter(integration -> EMAIL_INTEGRATION_NAME.equals(integration.getName()))
          .flatMap(mailServiceFactory::getDefaultEmailService)
          .ifPresentOrElse(emailService -> sendEmail(launchFinishedEvent, emailService),
              () -> LOGGER.warn("Unable to find {} integration for project {}",
                  IntegrationGroupEnum.NOTIFICATION, launchFinishedEvent.getProjectId()
              )
          );

    }

  }

  /**
   * Try to send email when it is needed
   *
   * @param emailService Mail Service
   */
  private void sendEmail(LaunchFinishedEvent launchFinishedEvent, EmailService emailService) {

    final Launch launch = getLaunchHandler.get(launchFinishedEvent.getId());
    final Project project = getProjectHandler.get(launch.getProjectId());

    project.getSenderCases().stream().filter(SenderCase::isEnabled).forEach(ec -> {
      SendCase sendCase = ec.getSendCase();
      boolean successRate = isSuccessRateEnough(launch, sendCase);
      boolean matchedNames = isLaunchNameMatched(launch, ec);
      boolean matchedTags =
          isAttributesMatched(launch, ec.getLaunchAttributeRules(), ec.getAttributesOperator());

      Set<String> recipients = ec.getRecipients();
      if (successRate && matchedNames && matchedTags) {
        String[] recipientsArray = findRecipients(
            userRepository.findLoginById(launch.getUserId()).orElseThrow(
                () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, launch.getUserId())),
            recipients
        );
        try {
          if (launchFinishedEvent.getBaseUrl() != null) {
            emailService.sendLaunchFinishNotification(recipientsArray,
                String.format("%s/ui/#%s", launchFinishedEvent.getBaseUrl(), project.getName()),
                project, launch
            );
          } else {
            emailService.sendLaunchFinishNotification(
                recipientsArray, String.format("/ui/#%s", project.getName()), project, launch);
          }
        } catch (Exception e) {
          LOGGER.error("Unable to send email.", e);
        }
      }
    });

  }

  private String[] findRecipients(String owner, Set<String> recipients) {
    return recipients.stream().map(recipient -> {
      if (recipient.contains("@")) {
        return recipient;
      } else {
        String toFind = recipient.equals(ProjectUtils.getOwner()) ? owner : recipient;
        Optional<User> user = userRepository.findByLogin(toFind);
        return user.map(User::getEmail).orElse(null);
      }
    }).filter(Objects::nonNull).distinct().toArray(String[]::new);
  }

  /**
   * @param launch launch to be evaluated
   * @return success rate of provided launch in %
   */
  private static double getSuccessRate(Launch launch) {
    double ti =
        extractStatisticsCount(DEFECTS_TO_INVESTIGATE_TOTAL, launch.getStatistics()).doubleValue();
    double pb =
        extractStatisticsCount(DEFECTS_PRODUCT_BUG_TOTAL, launch.getStatistics()).doubleValue();
    double si =
        extractStatisticsCount(DEFECTS_SYSTEM_ISSUE_TOTAL, launch.getStatistics()).doubleValue();
    double ab =
        extractStatisticsCount(DEFECTS_AUTOMATION_BUG_TOTAL, launch.getStatistics()).doubleValue();
    double total = extractStatisticsCount(EXECUTIONS_TOTAL, launch.getStatistics()).doubleValue();
    return total == 0 ? total : (ti + pb + si + ab) / total;
  }

  /**
   * @param launch Launch to be evaluated
   * @param option SendCase option
   * @return TRUE of success rate is enough for notification
   */
  private boolean isSuccessRateEnough(Launch launch, SendCase option) {
    switch (option) {
      case ALWAYS:
        return true;
      case FAILED:
        return getLaunchHandler.hasItemsWithIssues(launch);
      case TO_INVESTIGATE:
        return extractStatisticsCount(DEFECTS_TO_INVESTIGATE_TOTAL, launch.getStatistics()) > 0;
      case MORE_10:
        return getSuccessRate(launch) > 0.1;
      case MORE_20:
        return getSuccessRate(launch) > 0.2;
      case MORE_50:
        return getSuccessRate(launch) > 0.5;
      default:
        return false;
    }
  }

  /**
   * Validate matching of finished launch name and project settings for emailing
   *
   * @param launch  Launch to be evaluated
   * @param oneCase Mail case
   * @return TRUE if launch name matched
   */
  private static boolean isLaunchNameMatched(Launch launch, SenderCase oneCase) {
    Set<String> configuredNames = oneCase.getLaunchNames();
    return (null == configuredNames) || (configuredNames.isEmpty()) || configuredNames.contains(
        launch.getName());
  }

  /**
   * Validate matching of finished launch tags and project settings for emailing
   *
   * @param launch Launch to be evaluated
   * @return TRUE if tags matched
   */
  @VisibleForTesting
  private static boolean isAttributesMatched(Launch launch,
      Set<LaunchAttributeRule> launchAttributeRules, LogicalOperator logicalOperator) {

    if (CollectionUtils.isEmpty(launchAttributeRules)) {
      return true;
    }

    Set<ItemAttributeResource> itemAttributesResource =
        launchAttributeRules.stream().map(NotificationConfigConverter.TO_ATTRIBUTE_RULE_RESOURCE)
            .collect(Collectors.toSet());

    Set<ItemAttributeResource> itemAttributes =
        launch.getAttributes().stream().filter(attribute -> !attribute.isSystem())
            .map(attribute -> {
              ItemAttributeResource attributeResource = new ItemAttributeResource();
              attributeResource.setKey(attribute.getKey());
              attributeResource.setValue(attribute.getValue());
              return attributeResource;
            }).collect(Collectors.toSet());

    if (LogicalOperator.AND.equals(logicalOperator)) {
      return itemAttributesResource.stream().allMatch(resourceAttr -> itemAttributes.stream()
          .anyMatch(attr -> areAttributesMatched(attr, resourceAttr)));
    }

    return itemAttributes.stream().anyMatch(attr -> itemAttributesResource.stream()
        .anyMatch(resourceAttr -> areAttributesMatched(attr, resourceAttr)));
  }

  private static boolean areAttributesMatched(ItemAttributeResource itemAttribute,
      ItemAttributeResource itemAttributeResource) {
    // Case 1: Key and Value are the same
    boolean isEqual =
        Objects.equals(itemAttribute.getKey(), itemAttributeResource.getKey()) && Objects.equals(
            itemAttribute.getValue(), itemAttributeResource.getValue());

    // Case 2: Key is null in itemAttributesResource and the Value is the same
    boolean isValueEqualWithKeyNull =
        itemAttributeResource.getKey() == null && Objects.equals(itemAttribute.getValue(),
            itemAttributeResource.getValue()
        );

    return isEqual || isValueEqualWithKeyNull;
  }
}
