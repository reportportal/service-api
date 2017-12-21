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
package com.epam.ta.reportportal.util.email;

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.database.entity.user.UserUtils;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

/**
 * Email Sending Service based on {@link JavaMailSender}
 *
 * @author Andrei_Ramanchuk
 */
public class EmailService extends JavaMailSenderImpl {

	private static final String FINISH_LAUNCH_EMAIL_SUBJECT = " Report Portal Notification: launch '%s' #%s finished";
	private static final String URL_FORMAT = "%s/launches/all";
	private static final String FILTER_TAG_FORMAT = "%s?filter.has.tags=%s";
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
	public void sendLaunchFinishNotification(final String[] recipients, final String url, final Launch launch,
			final Project.Configuration settings) {
		String subject = format(FINISH_LAUNCH_EMAIL_SUBJECT, launch.getName(), launch.getNumber());
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);
			setFrom(message);

			String text = mergeFinishLaunchText(url, launch, settings);
			message.setText(text, true);

			attachSocialImages(message);
		};
		this.send(preparator);
	}

	@VisibleForTesting
	String mergeFinishLaunchText(String url, Launch launch, Project.Configuration settings) {
		Map<String, Object> email = new HashMap<>();
			/* Email fields values */
		String basicUrl = format(URL_FORMAT, url);
		email.put("name", launch.getName());
		email.put("number", String.valueOf(launch.getNumber()));
		email.put("description", launch.getDescription());
		email.put("url", format("%s/%s", basicUrl, launch.getId()));

			/* Tags with links */
		if (!CollectionUtils.isEmpty(launch.getTags())) {
			email.put("tags", launch.getTags()
					.stream()
					.collect(toMap(tag -> tag, tag -> format(FILTER_TAG_FORMAT, basicUrl, urlPathSegmentEscaper().escape(tag)))));
		}

			/* Launch execution statistics */
		email.put("total", launch.getStatistics().getExecutionCounter().getTotal().toString());
		email.put("passed", launch.getStatistics().getExecutionCounter().getPassed().toString());
		email.put("failed", launch.getStatistics().getExecutionCounter().getFailed().toString());
		email.put("skipped", launch.getStatistics().getExecutionCounter().getSkipped().toString());

			/* Launch issue statistics global counters */
		email.put("productBugTotal", launch.getStatistics().getIssueCounter().getProductBugTotal().toString());
		email.put("automationBugTotal", launch.getStatistics().getIssueCounter().getAutomationBugTotal().toString());
		email.put("systemIssueTotal", launch.getStatistics().getIssueCounter().getSystemIssueTotal().toString());
		email.put("noDefectTotal", launch.getStatistics().getIssueCounter().getNoDefectTotal().toString());
		email.put("toInvestigateTotal", launch.getStatistics().getIssueCounter().getToInvestigateTotal().toString());

			/* Launch issue statistics custom sub-types */
		if (launch.getStatistics().getIssueCounter().getProductBug().entrySet().size() > 1) {
			Map<StatisticSubType, String> pb = new LinkedHashMap<>();
			launch.getStatistics().getIssueCounter().getProductBug().forEach((k, v) -> {
				if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL)) {
					pb.put(settings.getByLocator(k), v.toString());
				}
			});
			email.put("pbInfo", pb);
		}
		if (launch.getStatistics().getIssueCounter().getAutomationBug().entrySet().size() > 1) {
			Map<StatisticSubType, String> ab = new LinkedHashMap<>();
			launch.getStatistics().getIssueCounter().getAutomationBug().forEach((k, v) -> {
				if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL)) {
					ab.put(settings.getByLocator(k), v.toString());
				}
			});
			email.put("abInfo", ab);
		}
		if (launch.getStatistics().getIssueCounter().getSystemIssue().entrySet().size() > 1) {
			Map<StatisticSubType, String> si = new LinkedHashMap<>();
			launch.getStatistics().getIssueCounter().getSystemIssue().forEach((k, v) -> {
				if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL)) {
					si.put(settings.getByLocator(k), v.toString());
				}
			});
			email.put("siInfo", si);
		}
		if (launch.getStatistics().getIssueCounter().getNoDefect().entrySet().size() > 1) {
			Map<StatisticSubType, String> nd = new LinkedHashMap<>();
			launch.getStatistics().getIssueCounter().getNoDefect().forEach((k, v) -> {
				if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL)) {
					nd.put(settings.getByLocator(k), v.toString());
				}
			});
			email.put("ndInfo", nd);
		}
		if (launch.getStatistics().getIssueCounter().getToInvestigate().entrySet().size() > 1) {
			Map<StatisticSubType, String> ti = new LinkedHashMap<>();
			launch.getStatistics().getIssueCounter().getToInvestigate().forEach((k, v) -> {
				if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL)) {
					ti.put(settings.getByLocator(k), v.toString());
				}
			});
			email.put("tiInfo", ti);
		}

		return templateEngine.merge("finish-launch-template.ftl", email);
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
