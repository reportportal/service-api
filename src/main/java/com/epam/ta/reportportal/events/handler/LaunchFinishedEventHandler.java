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
package com.epam.ta.reportportal.events.handler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.epam.ta.reportportal.commons.SendCase;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.FailReferenceResource;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.events.LaunchFinishedEvent;
import com.epam.ta.reportportal.util.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;
import com.google.common.annotations.VisibleForTesting;

/**
 * @author Andrei Varabyeu
 */
@Component
public class LaunchFinishedEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LaunchFinishedEventHandler.class);

	private final ServerSettingsRepository settingsRepository;

	private final FailReferenceResourceRepository issuesRepository;

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	private final ProjectSettingsRepository projectSettingsRepository;

	private final IIssuesAnalyzer analyzerService;

	private final EmailService emailService;

	private final UserRepository userRepository;

	private final Provider<HttpServletRequest> currentRequest;

	@Autowired
	public LaunchFinishedEventHandler(IIssuesAnalyzer analyzerService, ProjectSettingsRepository projectSettingsRepository,
			UserRepository userRepository, TestItemRepository testItemRepository, Provider<HttpServletRequest> currentRequest,
			LaunchRepository launchRepository, EmailService emailService, FailReferenceResourceRepository issuesRepository,
			ServerSettingsRepository settingsRepository) {
		this.analyzerService = analyzerService;
		this.projectSettingsRepository = projectSettingsRepository;
		this.userRepository = userRepository;
		this.testItemRepository = testItemRepository;
		this.currentRequest = currentRequest;
		this.launchRepository = launchRepository;
		this.emailService = emailService;
		this.issuesRepository = issuesRepository;
		this.settingsRepository = settingsRepository;
	}

	@EventListener
	public void onApplicationEvent(LaunchFinishedEvent event) {
		afterFinishLaunch(event.getProject(), event.getLaunch());
	}

	private void afterFinishLaunch(Project project, Launch launch) { // NOSONAR

		/* Should we send email right now or wait till AA is finished? */
		boolean shouldSendIt = false;

		/*
		 * If server settings profiling will be added - update would be required
		 * for profile ID
		 */
		ServerEmailConfig emailConfig = settingsRepository.findOne("default").getServerEmailConfig();
		boolean emailEnabled;
		try {
			emailService.reconfig(emailConfig);
			emailService.testConnection();
			emailEnabled = true;
		} catch (Exception e) {
			/* Something wrong with email service or remote server */
			LOGGER.error("Email configuration exception!", e);
			emailEnabled = false;
		}

		/* Avoid NULL object processing */
		if (null == project || null == launch)
			return;

		/* If AA enabled then waiting results processing */
		if (project.getConfiguration().getIsAutoAnalyzerEnabled()) {
			shouldSendIt = true;
		}

		/* If email enabled and AA disabled then send results immediately */
		if (emailEnabled && project.getConfiguration().getEmailConfig().getEmailEnabled() && !shouldSendIt) {
			sendEmailRightNow(launch, project, emailConfig);
			shouldSendIt = false;
		}

		// Do not process debug launches.
		if (launch.getMode().equals(Mode.DEBUG))
			return;
		List<FailReferenceResource> resources = issuesRepository.findAllLaunchIssues(launch.getId());
		if (!project.getConfiguration().getIsAutoAnalyzerEnabled()) {
			this.clearInvestigatedIssues(resources);
			return;
		}
		List<TestItem> previous = analyzerService.collectPreviousIssues(5, launch.getId(), project.getName());
		List<TestItem> converted = resources.stream().map(resource -> testItemRepository.findOne(resource.getTestItemRef()))
				.collect(Collectors.toList());
		analyzerService.analyze(launch.getId(), converted, previous);

		// Remove already processed items from repository
		this.clearInvestigatedIssues(resources);

		/* Previous email sending cycle was skipped due waiting AA results */
		if (emailEnabled && project.getConfiguration().getEmailConfig().getEmailEnabled() && shouldSendIt) {
			// Get launch with AA results
			launch = launchRepository.findOne(launch.getId());
			sendEmailRightNow(launch, project, emailConfig);
		}
	}

	/**
	 * Clear failReferences repository
	 *
	 * @param issues
	 */
	private void clearInvestigatedIssues(List<FailReferenceResource> issues) {
		issuesRepository.delete(issues);
	}

	/**
	 * Calculate success rate of provided launch in %
	 *
	 * @param launch
	 * @return
	 */
	static double getSuccessRate(Launch launch) {
		Double ti = launch.getStatistics().getIssueCounter().getToInvestigateTotal().doubleValue();
		Double pb = launch.getStatistics().getIssueCounter().getProductBugTotal().doubleValue();
		Double si = launch.getStatistics().getIssueCounter().getSystemIssueTotal().doubleValue();
		Double ab = launch.getStatistics().getIssueCounter().getAutomationBugTotal().doubleValue();
		Double total = launch.getStatistics().getExecutionCounter().getTotal().doubleValue();
		return total == 0 ? total : (ti + pb + si + ab) / total;
	}

	/**
	 * Check if success rate is enough for notification
	 *
	 * @param launch
	 * @param option
	 * @return
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
	 * Validate matching of finished launch name and project settings for
	 * emailing
	 *
	 * @param launch
	 * @param oneCase
	 * @return
	 */
	static boolean isLaunchNameMatched(Launch launch, EmailSenderCase oneCase) {
		List<String> configuredNames = oneCase.getLaunchNames();
		return (null == configuredNames) || (configuredNames.isEmpty()) || configuredNames.contains(launch.getName());
	}

	/**
	 * Validate matching of finished launch tags and project settings for
	 * emailing
	 *
	 * @param launch
	 * @param oneCase
	 * @return
	 */
	@VisibleForTesting
	static boolean isTagsMatched(Launch launch, EmailSenderCase oneCase) {
		return !(null != oneCase.getTags() && !oneCase.getTags().isEmpty()) || null != launch.getTags() && oneCase.getTags().containsAll(launch.getTags());
	}

	/**
	 * Try to send email when it is needed
	 *
	 * @param launch
	 * @param project
	 */
	void sendEmailRightNow(Launch launch, Project project, ServerEmailConfig emailConfig) {
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
							.replacePath(String.format("/#%s/launches/all/", project.getName())).build().toUriString();

					String resourcesURL = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(currentRequest.get()))
							.replacePath("/img").build().toUriString();

					ProjectSettings settings = projectSettingsRepository.findOne(launch.getProjectRef());
					emailService.reconfig(emailConfig);
					emailService.setAddressFrom(project.getConfiguration().getEmailConfig().getFrom());
					emailService.sendLaunchFinishNotification(recipientsArray, basicURL + launch.getId(), launch, resourcesURL, settings);
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
