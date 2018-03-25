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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.commons.SendCase;
import com.epam.ta.reportportal.core.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.launch.IRetriesLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.events.LaunchFinishedEvent;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Andrei Varabyeu
 */
@Component
public class LaunchFinishedEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LaunchFinishedEventHandler.class);

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	private final IIssuesAnalyzer analyzerService;

	private final ILogIndexer logIndexer;

	private final IRetriesLaunchHandler retriesLaunchHandler;

	private final MailServiceFactory emailServiceFactory;

	private final UserRepository userRepository;

	private final Provider<HttpServletRequest> currentRequest;

	@Autowired
	public LaunchFinishedEventHandler(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			IIssuesAnalyzer analyzerService, ILogIndexer logIndexer, IRetriesLaunchHandler retriesLaunchHandler,
			MailServiceFactory emailServiceFactory, UserRepository userRepository, Provider<HttpServletRequest> currentRequest) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.analyzerService = analyzerService;
		this.logIndexer = logIndexer;
		this.retriesLaunchHandler = retriesLaunchHandler;
		this.emailServiceFactory = emailServiceFactory;
		this.userRepository = userRepository;
		this.currentRequest = currentRequest;
	}

	@EventListener
	public void onApplicationEvent(LaunchFinishedEvent event) {
		afterFinishLaunch(event.getProject(), event.getLaunch());
	}

	private void afterFinishLaunch(final Project project, final Launch launch) {
		/* Avoid NULL object processing */
		if (null == project || null == launch) {
			return;
		}

		retriesLaunchHandler.handleRetries(launch);
		Optional<EmailService> emailService = emailServiceFactory.getDefaultEmailService(project.getConfiguration().getEmailConfig());

		// Do not process debug launches.
		if (Mode.DEBUG.equals(launch.getMode())) {
			return;
		}
		/* Index items in issue for AA */
		logIndexer.indexLogs(launch.getId(), testItemRepository.findTestItemWithIssues(launch.getId()));

		/* If email enabled and AA disabled then send results immediately */
		if (!BooleanUtils.toBoolean(project.getConfiguration().getIsAutoAnalyzerEnabled())) {
			emailService.ifPresent(service -> sendEmailRightNow(launch, project, service));
			return;
		}

		List<TestItem> toInvestigateItems = testItemRepository.findInIssueTypeItems(TestItemIssueType.TO_INVESTIGATE.getLocator(),
				launch.getId()
		);
		analyzerService.analyze(launch, toInvestigateItems,
				Optional.ofNullable(project.getConfiguration().getAnalyzerMode()).orElse(AnalyzeMode.BY_LAUNCH_NAME)
		);
		// Get launch with AA results
		Launch freshLaunch = launchRepository.findOne(launch.getId());
		emailService.ifPresent(it -> sendEmailRightNow(freshLaunch, project, it));
	}

	/**
	 * @param launch launch to be evaluated
	 * @return success rate of provided launch in %
	 */
	private static double getSuccessRate(Launch launch) {
		Double ti = launch.getStatistics().getIssueCounter().getToInvestigateTotal().doubleValue();
		Double pb = launch.getStatistics().getIssueCounter().getProductBugTotal().doubleValue();
		Double si = launch.getStatistics().getIssueCounter().getSystemIssueTotal().doubleValue();
		Double ab = launch.getStatistics().getIssueCounter().getAutomationBugTotal().doubleValue();
		Double total = launch.getStatistics().getExecutionCounter().getTotal().doubleValue();
		return total == 0 ? total : (ti + pb + si + ab) / total;
	}

	/**
	 * @param launch Launch to be evaluated
	 * @param option SendCase option
	 * @return TRUE of success rate is enough for notification
	 */
	static boolean isSuccessRateEnough(Launch launch, SendCase option) {
		switch (option) {
			case ALWAYS:
				return true;
			case FAILED:
				return launch.getStatus().equals(Status.FAILED);
			case TO_INVESTIGATE:
				return launch.getStatistics().getIssueCounter().getToInvestigateTotal() > 0;
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
	static boolean isLaunchNameMatched(Launch launch, EmailSenderCase oneCase) {
		List<String> configuredNames = oneCase.getLaunchNames();
		return (null == configuredNames) || (configuredNames.isEmpty()) || configuredNames.contains(launch.getName());
	}

	/**
	 * Validate matching of finished launch tags and project settings for emailing
	 *
	 * @param launch  Launch to be evaluated
	 * @param oneCase Mail case
	 * @return TRUE if tags matched
	 */
	@VisibleForTesting
	static boolean isTagsMatched(Launch launch, EmailSenderCase oneCase) {
		return !(null != oneCase.getTags() && !oneCase.getTags().isEmpty()) || null != launch.getTags() && launch.getTags()
				.containsAll(oneCase.getTags());
	}

	/**
	 * Try to send email when it is needed
	 *
	 * @param launch       Launch to be used
	 * @param project      Project to be used
	 * @param emailService Mail Service
	 */
	void sendEmailRightNow(Launch launch, Project project, EmailService emailService) {
		ProjectEmailConfig projectConfig = project.getConfiguration().getEmailConfig();
		for (EmailSenderCase one : projectConfig.getEmailCases()) {
			Optional<SendCase> option = SendCase.findByName(one.getSendCase());
			boolean successRate = isSuccessRateEnough(launch, option.get());
			boolean matchedNames = isLaunchNameMatched(launch, one);
			boolean matchedTags = isTagsMatched(launch, one);
			List<String> recipients = one.getRecipients();
			if (successRate && matchedNames && matchedTags) {
				String[] recipientsArray = findRecipients(launch.getUserRef(), recipients);
				try {
					/* Update with static Util resources provider */
					String basicURL = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(currentRequest.get()))
							.replacePath(String.format("/#%s", project.getName()))
							.build()
							.toUriString();

					emailService.sendLaunchFinishNotification(recipientsArray, basicURL, launch, project.getConfiguration());
				} catch (Exception e) {
					LOGGER.error("Unable to send email. Error: \n{}", e);
				}
			}
		}
	}

	String[] findRecipients(String owner, List<String> recipients) {
		return recipients.stream().map(recipient -> {
			if (recipient.contains("@")) {
				return recipient;
			} else {
				String toFind = recipient.equals(ProjectUtils.getOwner()) ? owner : recipient;
				User user = userRepository.findOne(toFind);
				if (user != null) {
					return user.getEmail();
				}
				return null;
			}
		}).filter(Objects::nonNull).distinct().toArray(String[]::new);
	}

}
