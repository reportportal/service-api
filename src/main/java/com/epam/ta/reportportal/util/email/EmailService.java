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
package com.epam.ta.reportportal.util.email;

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.util.email.constant.IssueRegexConstant;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * Email Sending Service based on {@link JavaMailSender}
 *
 * @author Andrei_Ramanchuk
 */
public class EmailService extends JavaMailSenderImpl {

	private static final String FINISH_LAUNCH_EMAIL_SUBJECT = " Report Portal Notification: [%s] launch '%s' #%s finished";
	private static final String URL_FORMAT = "%s/launches/all";
	private static final String FILTER_TAG_FORMAT = "%s?filter.has.key=%s&filter.has.value=%s";
	private static final String EMAIL_TEMPLATE_PREFIX = "templates/email/";
	private TemplateEngine templateEngine;

	/* Default value for FROM project notifications field */
	private String from;

	public EmailService(Properties javaMailProperties) {
		super.setJavaMailProperties(javaMailProperties);
	}

	/**
	 * User creation confirmation email
	 *
	 * @param subject    Letter's subject
	 * @param recipients Letter's recipients
	 * @param url        ReportPortal URL
	 */
	public void sendCreateUserConfirmationEmail(final String subject, final String[] recipients, final String url) {
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);
			setFrom(message);

			Map<String, Object> email = new HashMap<>();
			email.put("url", url);
			String text = templateEngine.merge("registration-template.ftl", email);
			message.setText(text, true);

			message.addInline("create-user.png", emailTemplateResource("create-user.png"));

			attachSocialImages(message);
		};
		this.send(preparator);
	}

	/**
	 * Finish launch notification
	 *
	 * @param recipients List of recipients
	 * @param url        ReportPortal URL
	 * @param launch     Launch
	 */
	public void sendLaunchFinishNotification(final String[] recipients, final String url, final String projectName, final Launch launch) {
		String subject = format(FINISH_LAUNCH_EMAIL_SUBJECT, projectName.toUpperCase(), launch.getName(), launch.getNumber());
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);
			setFrom(message);

			String text = mergeFinishLaunchText(url, launch);
			message.setText(text, true);

			attachSocialImages(message);
		};
		this.send(preparator);
	}

	@VisibleForTesting
	String mergeFinishLaunchText(String url, Launch launch) {
		Map<String, Object> email = new HashMap<>();
		/* Email fields values */
		String basicUrl = format(URL_FORMAT, url);
		email.put("name", launch.getName());
		email.put("number", String.valueOf(launch.getNumber()));
		email.put("description", launch.getDescription());
		email.put("url", format("%s/%s", basicUrl, launch.getId()));

		/* Tags with links */
		if (!CollectionUtils.isEmpty(launch.getAttributes())) {
			email.put(
					"attributes",
					launch.getAttributes().stream().collect(toMap(tag -> tag.getKey().concat(":").concat(tag.getValue()), tag -> format(
							FILTER_TAG_FORMAT,
							basicUrl,
							urlPathSegmentEscaper().escape(tag.getKey()),
							urlPathSegmentEscaper().escape(tag.getValue())
					)))
			);
		}

		/* Launch execution statistics */

		Map<String, Integer> statistics = launch.getStatistics()
				.stream()
				.filter(s -> ofNullable(s.getStatisticsField()).isPresent() && StringUtils.isNotEmpty(s.getStatisticsField().getName()))
				.collect(Collectors.toMap(s -> s.getStatisticsField().getName(), Statistics::getCounter, (prev, curr) -> prev));

		email.put("total", ofNullable(statistics.get("statistics$executions$total")).orElse(0));
		email.put("passed", ofNullable(statistics.get("statistics$executions$passed")).orElse(0));
		email.put("failed", ofNullable(statistics.get("statistics$executions$failed")).orElse(0));
		email.put("skipped", ofNullable(statistics.get("statistics$executions$skipped")).orElse(0));

		/* Launch issue statistics global counters */
		email.put("productBugTotal", ofNullable(statistics.get("statistics$product_bug$total")).orElse(0));
		email.put("automationBugTotal", ofNullable(statistics.get("statistics$product_bug$total")).orElse(0));
		email.put("systemIssueTotal", ofNullable(statistics.get("statistics$system_issue$total")).orElse(0));
		email.put("noDefectTotal", ofNullable(statistics.get("statistics$no_defect$total")).orElse(0));
		email.put("toInvestigateTotal", ofNullable(statistics.get("statistics$to_investigate$total")).orElse(0));



		/* Launch issue statistics custom sub-types */
		fillEmail(email, "pbInfo", statistics, IssueRegexConstant.PRODUCT_BUG_ISSUE_REGEX);
		fillEmail(email, "abInfo", statistics, IssueRegexConstant.AUTOMATION_BUG_ISSUE_REGEX);
		fillEmail(email, "siInfo", statistics, IssueRegexConstant.SYSTEM_ISSUE_REGEX);
		fillEmail(email, "ndInfo", statistics, IssueRegexConstant.NO_DEFECT_ISSUE_REGEX);
		fillEmail(email, "tiInfo", statistics, IssueRegexConstant.TO_INVESTIGATE_ISSUE_REGEX);

		return templateEngine.merge("finish-launch-template.ftl", email);
	}

	private void fillEmail(Map<String, Object> email, String statisticsName, Map<String, Integer> statistics, String regex) {
		Optional<Map<String, Integer>> pb = ofNullable(statistics.entrySet().stream().filter(entry -> {
			Pattern pattern = Pattern.compile(regex);
			return pattern.matcher(entry.getKey()).matches();
		}).collect(Collectors.toMap(Map.Entry::getKey, entry -> ofNullable(entry.getValue()).orElse(0), (prev, curr) -> prev)));

		pb.ifPresent(stats -> email.put(statisticsName, stats));
	}

	/**
	 * Restore password email
	 *
	 * @param subject    Letter's subject
	 * @param recipients Letter's recipients
	 * @param url        ReportPortal URL
	 * @param login      User's login
	 */
	public void sendRestorePasswordEmail(final String subject, final String[] recipients, final String url, final String login) {
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);

			setFrom(message);

			Map<String, Object> email = new HashMap<>();
			email.put("login", login);
			email.put("url", url);
			String text = templateEngine.merge("restore-password-template.ftl", email);
			message.setText(text, true);

			message.addInline("restore-password.png", emailTemplateResource("restore-password.png"));
			attachSocialImages(message);
		};
		this.send(preparator);
	}

	public void sendIndexFinishedEmail(final String subject, final String recipient, final Long indexedLogsCount) {
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipient);
			Map<String, String> email = new HashMap<>();
			email.put("indexedLogsCount", String.valueOf(ofNullable(indexedLogsCount).orElse(0L)));
			setFrom(message);
			String text = templateEngine.merge("index-finished-template.ftl", email);
			message.setText(text, true);
		};
		this.send(preparator);
	}

	public void setTemplateEngine(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void sendCreateUserConfirmationEmail(CreateUserRQFull req, String basicUrl) {
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject("Welcome to Report Portal");
			message.setTo(req.getEmail());
			setFrom(message);

			Map<String, Object> email = new HashMap<>();
			email.put("url", basicUrl);
			email.put("login", normalizeId(req.getLogin()));
			email.put("password", req.getPassword());
			String text = templateEngine.merge("create-user-template.ftl", email);
			message.setText(text, true);

			message.addInline("create-user.png", emailTemplateResource("create-user.png"));
			attachSocialImages(message);
		};
		this.send(preparator);
	}

	/**
	 * Builds FROM field
	 * If username is email, format will be "from \<email\>"
	 */
	private void setFrom(MimeMessageHelper message) throws MessagingException, UnsupportedEncodingException {
		if (!Strings.isNullOrEmpty(this.from)) {
			if (isAddressValid(this.from)) {
				message.setFrom(this.from);
			} else if (UserUtils.isEmailValid(getUsername())) {
				message.setFrom(getUsername(), this.from);
			}
		}
		//otherwise generate automatically
	}

	private boolean isAddressValid(String from) {
		try {
			InternetAddress.parse(from);
			return true;
		} catch (AddressException e) {
			return false;
		}
	}

	private void attachSocialImages(MimeMessageHelper message) throws MessagingException {
		message.addInline("ic-github.png", emailTemplateResource("ic-github.png"));
		message.addInline("ic-fb.png", emailTemplateResource("ic-fb.png"));
		message.addInline("ic-twitter.png", emailTemplateResource("ic-twitter.png"));
		message.addInline("ic-youtube.png", emailTemplateResource("ic-youtube.png"));
		message.addInline("ic-vk.png", emailTemplateResource("ic-vk.png"));
		message.addInline("ic-slack.png", emailTemplateResource("ic-slack.png"));
	}

	private ClassPathResource emailTemplateResource(String resource) {
		return new ClassPathResource(EMAIL_TEMPLATE_PREFIX + resource);
	}
}
