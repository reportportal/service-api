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

package com.epam.ta.reportportal.core.events.handler.subscriber.impl;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.subscriber.LaunchFinishedEventSubscriber;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.extractStatisticsCount;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchNotificationSubscriber implements LaunchFinishedEventSubscriber {

	public static final Logger LOGGER = LoggerFactory.getLogger(LaunchNotificationSubscriber.class);

	private final GetIntegrationHandler getIntegrationHandler;
	private final MailServiceFactory mailServiceFactory;
	private final LaunchRepository launchRepository;
	private final UserRepository userRepository;

	@Autowired
	public LaunchNotificationSubscriber(GetIntegrationHandler getIntegrationHandler, MailServiceFactory mailServiceFactory,
			LaunchRepository launchRepository, UserRepository userRepository) {
		this.getIntegrationHandler = getIntegrationHandler;
		this.mailServiceFactory = mailServiceFactory;
		this.launchRepository = launchRepository;
		this.userRepository = userRepository;
	}

	@Override
	public void handleEvent(LaunchFinishedEvent launchFinishedEvent, Project project, Launch launch) {

		boolean isNotificationsEnabled = BooleanUtils.toBoolean(ProjectUtils.getConfigParameters(project.getProjectAttributes())
				.get(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute()));

		if (isNotificationsEnabled) {
			Integration emailIntegration = getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
					IntegrationGroupEnum.NOTIFICATION
			).orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, "EMAIL"));
			Optional<EmailService> emailService = mailServiceFactory.getDefaultEmailService(emailIntegration);
			emailService.ifPresent(it -> {
				launchRepository.refresh(launch);
				sendEmail(launch, project, it, launchFinishedEvent.getBaseUrl());
			});
		}

	}

	/**
	 * Try to send email when it is needed
	 *
	 * @param launch       Launch to be used
	 * @param project      Project
	 * @param emailService Mail Service
	 */
	private void sendEmail(Launch launch, Project project, EmailService emailService, String baseUrl) {

		project.getSenderCases().stream().filter(SenderCase::isEnabled).forEach(ec -> {
			SendCase sendCase = ec.getSendCase();
			boolean successRate = isSuccessRateEnough(launch, sendCase);
			boolean matchedNames = isLaunchNameMatched(launch, ec);
			boolean matchedTags = isAttributesMatched(launch, ec.getLaunchAttributeRules());

			Set<String> recipients = ec.getRecipients();
			if (successRate && matchedNames && matchedTags) {
				String[] recipientsArray = findRecipients(userRepository.findLoginById(launch.getUserId())
						.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, launch.getUserId())), recipients);
				try {
					emailService.sendLaunchFinishNotification(recipientsArray,
							String.format("%s/ui/#%s", baseUrl, project.getName()),
							project,
							launch
					);
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
		double ti = extractStatisticsCount(DEFECTS_TO_INVESTIGATE_TOTAL, launch.getStatistics()).doubleValue();
		double pb = extractStatisticsCount(DEFECTS_PRODUCT_BUG_TOTAL, launch.getStatistics()).doubleValue();
		double si = extractStatisticsCount(DEFECTS_SYSTEM_ISSUE_TOTAL, launch.getStatistics()).doubleValue();
		double ab = extractStatisticsCount(DEFECTS_AUTOMATION_BUG_TOTAL, launch.getStatistics()).doubleValue();
		double total = extractStatisticsCount(EXECUTIONS_TOTAL, launch.getStatistics()).doubleValue();
		return total == 0 ? total : (ti + pb + si + ab) / total;
	}

	/**
	 * @param launch Launch to be evaluated
	 * @param option SendCase option
	 * @return TRUE of success rate is enough for notification
	 */
	private static boolean isSuccessRateEnough(Launch launch, SendCase option) {
		switch (option) {
			case ALWAYS:
				return true;
			case FAILED:
				return launch.getStatus().equals(StatusEnum.FAILED);
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
		return (null == configuredNames) || (configuredNames.isEmpty()) || configuredNames.contains(launch.getName());
	}

	/**
	 * Validate matching of finished launch tags and project settings for emailing
	 *
	 * @param launch Launch to be evaluated
	 * @return TRUE if tags matched
	 */
	@VisibleForTesting
	private static boolean isAttributesMatched(Launch launch, Set<LaunchAttributeRule> launchAttributeRules) {

		if (CollectionUtils.isEmpty(launchAttributeRules)) {
			return true;
		}

		return launch.getAttributes()
				.stream()
				.filter(attribute -> !attribute.isSystem())
				.map(attribute -> {
					ItemAttributeResource attributeResource = new ItemAttributeResource();
					attributeResource.setKey(attribute.getKey());
					attributeResource.setValue(attribute.getValue());
					return attributeResource;
				})
				.collect(Collectors.toSet())
				.containsAll(launchAttributeRules.stream()
						.map(NotificationConfigConverter.TO_ATTRIBUTE_RULE_RESOURCE)
						.collect(Collectors.toSet()));
	}

	@Override
	public int getOrder() {
		return 2;
	}
}
