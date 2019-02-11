/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.core.analyzer.IssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.epam.ta.reportportal.core.project.impl.StatisticsUtils.extractStatisticsCount;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static java.util.stream.Collectors.toList;

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

	private final IssuesAnalyzer issuesAnalyzer;

	private final LogIndexer logIndexer;

	@Autowired
	public LaunchFinishedEventHandler(ProjectRepository projectRepository, GetIntegrationHandler getIntegrationHandler,
			MailServiceFactory mailServiceFactory, UserRepository userRepository, LaunchRepository launchRepository,
			Provider<HttpServletRequest> currentRequest, AnalyzeCollectorFactory analyzeCollectorFactory, IssuesAnalyzer issuesAnalyzer,
			LogIndexer logIndexer) {
		this.projectRepository = projectRepository;
		this.getIntegrationHandler = getIntegrationHandler;
		this.mailServiceFactory = mailServiceFactory;
		this.userRepository = userRepository;
		this.launchRepository = launchRepository;
		this.currentRequest = currentRequest;
		this.analyzeCollectorFactory = analyzeCollectorFactory;
		this.issuesAnalyzer = issuesAnalyzer;
		this.logIndexer = logIndexer;
	}

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
		logIndexer.indexLogs(project.getId(), Lists.newArrayList(launch.getId()), analyzerConfig);

		Integration emailIntegration = getIntegrationHandler.getEnabledByProjectIdOrGlobalAndIntegrationGroup(project.getId(),
				IntegrationGroupEnum.NOTIFICATION
		)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, "EMAIL"));
		Optional<EmailService> emailService = mailServiceFactory.getDefaultEmailService(emailIntegration);

		if (!BooleanUtils.isTrue(analyzerConfig.getIsAutoAnalyzerEnabled())) {
			emailService.ifPresent(it -> sendEmail(launch, project, it));
			return;
		}

		if (issuesAnalyzer.hasAnalyzers()) {
			List<Long> testItems = analyzeCollectorFactory.getCollector(AnalyzeItemsMode.TO_INVESTIGATE)
					.collectItems(project.getId(), launch.getId(), null);

			CompletableFuture<Void> analyze = issuesAnalyzer.analyze(launch, testItems, analyzerConfig);

			analyze.thenAccept(res -> {
				Launch updatedLaunch = launchRepository.findById(launch.getId())
						.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launch.getId()));
				emailService.ifPresent(it -> sendEmail(updatedLaunch, project, it));
			});
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
	private static boolean isAttributesMatched(Launch launch, Set<String> attributes) {
		return !(null != attributes && !attributes.isEmpty()) || null != launch.getAttributes() && launch.getAttributes()
				.stream()
				.map(ItemAttribute::getKey)
				.collect(toList())
				.containsAll(attributes);
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
			boolean matchedTags = isAttributesMatched(launch, ec.getLaunchAttributes());

			Set<String> recipients = ec.getRecipients();
			if (successRate && matchedNames && matchedTags) {
				String[] recipientsArray = findRecipients(launch.getUser().getLogin(), recipients);
				try {
					/* Update with static Util resources provider */
					String basicURL = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(currentRequest.get()))
							.replacePath(String.format("/#%s", project.getName()))
							.build()
							.toUriString();

					emailService.sendLaunchFinishNotification(recipientsArray, basicURL, project.getName(), launch);
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
