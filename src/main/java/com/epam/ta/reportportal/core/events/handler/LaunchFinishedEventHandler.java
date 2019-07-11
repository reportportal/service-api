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

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.*;
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
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.extractStatisticsCount;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchFinishedEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LaunchFinishedEventHandler.class);

	private final ProjectRepository projectRepository;

	private final GetIntegrationHandler getIntegrationHandler;

	private final MailServiceFactory mailServiceFactory;

	private final UserRepository userRepository;

	private final LaunchRepository launchRepository;

	private final Provider<HttpServletRequest> currentRequest;

	private final AnalyzeCollectorFactory analyzeCollectorFactory;

	private final AnalyzerServiceAsync analyzerServiceAsync;

	private final LogIndexer logIndexer;

	private final PatternAnalyzer patternAnalyzer;

	@Autowired
	public LaunchFinishedEventHandler(ProjectRepository projectRepository, GetIntegrationHandler getIntegrationHandler,
			MailServiceFactory mailServiceFactory, UserRepository userRepository, LaunchRepository launchRepository,
			Provider<HttpServletRequest> currentRequest, AnalyzeCollectorFactory analyzeCollectorFactory,
			AnalyzerServiceAsync analyzerServiceAsync, LogIndexer logIndexer, PatternAnalyzer patternAnalyzer) {
		this.projectRepository = projectRepository;
		this.getIntegrationHandler = getIntegrationHandler;
		this.mailServiceFactory = mailServiceFactory;
		this.userRepository = userRepository;
		this.launchRepository = launchRepository;
		this.currentRequest = currentRequest;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
		this.analyzerServiceAsync = analyzerServiceAsync;
		this.logIndexer = logIndexer;
		this.patternAnalyzer = patternAnalyzer;
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void onApplicationEvent(LaunchFinishedEvent event) {
		Launch launch = launchRepository.findById(event.getLaunchActivityResource().getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, event.getLaunchActivityResource().getId()));

		if (LaunchModeEnum.DEBUG == launch.getMode()) {
			return;
		}
		Project project = projectRepository.findById(launch.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, launch.getProjectId()));

		AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(project);

		boolean isNotificationsEnabled = BooleanUtils.toBoolean(ProjectUtils.getConfigParameters(project.getProjectAttributes())
				.get(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute()));

		if (BooleanUtils.isTrue(analyzerConfig.getIsAutoAnalyzerEnabled()) && analyzerServiceAsync.hasAnalyzers()) {
			List<Long> itemIds = analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)
					.collectItems(project.getId(), launch.getId(), null);
			logIndexer.indexLaunchLogs(project.getId(), launch.getId(), analyzerConfig).join();
			analyzerServiceAsync.analyze(launch, itemIds, analyzerConfig).join();
			CompletableFuture.supplyAsync(() -> logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemIds, analyzerConfig));
		} else {
			logIndexer.indexLaunchLogs(project.getId(), launch.getId(), analyzerConfig);
		}

		if (isNotificationsEnabled) {
			Integration emailIntegration = getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
					IntegrationGroupEnum.NOTIFICATION
			)
					.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, "EMAIL"));
			Optional<EmailService> emailService = mailServiceFactory.getDefaultEmailService(emailIntegration);

			Launch updatedLaunch = launchRepository.findById(launch.getId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launch.getId()));
			emailService.ifPresent(it -> {
				launchRepository.refresh(updatedLaunch);
				sendEmail(updatedLaunch, project, it);
			});
		}

		boolean isPatternAnalysisEnabled = BooleanUtils.toBoolean(ProjectUtils.getConfigParameters(project.getProjectAttributes())
				.get(ProjectAttributeEnum.PATTERN_ANALYSIS_ENABLED.getAttribute()));

		if (isPatternAnalysisEnabled) {
			patternAnalyzer.analyzeTestItems(launch);
		}

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

	/**
	 * Try to send email when it is needed
	 *
	 * @param launch       Launch to be used
	 * @param project      Project
	 * @param emailService Mail Service
	 */
	private void sendEmail(Launch launch, Project project, EmailService emailService) {

		project.getSenderCases().forEach(ec -> {
			SendCase sendCase = ec.getSendCase();
			boolean successRate = isSuccessRateEnough(launch, sendCase);
			boolean matchedNames = isLaunchNameMatched(launch, ec);
			boolean matchedTags = isAttributesMatched(launch, ec.getLaunchAttributeRules());

			Set<String> recipients = ec.getRecipients();
			if (successRate && matchedNames && matchedTags) {
				String[] recipientsArray = findRecipients(launch.getUser().getLogin(), recipients);
				try {
					/* Update with static Util resources provider */
					String basicURL = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(currentRequest.get()))
							.replacePath(String.format("/#%s", project.getName()))
							.build()
							.toUriString();

					emailService.sendLaunchFinishNotification(recipientsArray, basicURL, project, launch);
				} catch (Exception e) {
					LOGGER.error("Unable to send email. Error: \n{}", e);
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

}
