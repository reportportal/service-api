/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeUsername;

/**
 * Email Sending Service based on {@link JavaMailSender}
 *
 * @author Andrei_Ramanchuk
 */
public class EmailService extends JavaMailSenderImpl {

	private static final String FINISH_LAUNCH_EMAIL_SUBJECT = " Report Portal Notification: launch '%s' #%s finished";
	private static final String LOGO = "templates/email/rp_logo.png";
	private TemplateEngine templateEngine;

	/*
	 * Static email FROM field for server level notifications. Put in ext.
	 * config?
	 */
	private static final String RP_EMAIL = "ReportPortal@service.com";

	/* Default value for FROM project notifications field */
	private String addressFrom = RP_EMAIL;

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
	public void sendConfirmationEmail(final String subject, final String[] recipients, final String url) {
		MimeMessagePreparator preparator = mimeMessage -> {
			URL logoImg = this.getClass().getClassLoader().getResource(LOGO);
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);
			message.setFrom(RP_EMAIL);
			Map<String, Object> email = new HashMap<>();
			email.put("url", url);
			String text = templateEngine.merge("registration-template.vm", email);
			message.setText(text, true);
			message.addInline("logoimg", new UrlResource(logoImg));
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
		String subject = String.format(FINISH_LAUNCH_EMAIL_SUBJECT, launch.getName(), launch.getNumber());
		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);
			message.setFrom(addressFrom);

			Map<String, Object> email = new HashMap<>();
			/* Email fields values */
			email.put("name", launch.getName());
			email.put("number", String.valueOf(launch.getNumber()));
			email.put("description", launch.getDescription());
			email.put("url", url);

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
					if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL))
						pb.put(settings.getByLocator(k), v.toString());
				});
				email.put("pbInfo", pb);
			}
			if (launch.getStatistics().getIssueCounter().getAutomationBug().entrySet().size() > 1) {
				Map<StatisticSubType, String> ab = new LinkedHashMap<>();
				launch.getStatistics().getIssueCounter().getAutomationBug().forEach((k, v) -> {
					if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL))
						ab.put(settings.getByLocator(k), v.toString());
				});
				email.put("abInfo", ab);
			}
			if (launch.getStatistics().getIssueCounter().getSystemIssue().entrySet().size() > 1) {
				Map<StatisticSubType, String> si = new LinkedHashMap<>();
				launch.getStatistics().getIssueCounter().getSystemIssue().forEach((k, v) -> {
					if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL))
						si.put(settings.getByLocator(k), v.toString());
				});
				email.put("siInfo", si);
			}
			if (launch.getStatistics().getIssueCounter().getNoDefect().entrySet().size() > 1) {
				Map<StatisticSubType, String> nd = new LinkedHashMap<>();
				launch.getStatistics().getIssueCounter().getNoDefect().forEach((k, v) -> {
					if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL))
						nd.put(settings.getByLocator(k), v.toString());
				});
				email.put("ndInfo", nd);
			}
			if (launch.getStatistics().getIssueCounter().getToInvestigate().entrySet().size() > 1) {
				Map<StatisticSubType, String> ti = new LinkedHashMap<>();
				launch.getStatistics().getIssueCounter().getToInvestigate().forEach((k, v) -> {
					if (!k.equalsIgnoreCase(IssueCounter.GROUP_TOTAL))
						ti.put(settings.getByLocator(k), v.toString());
				});
				email.put("tiInfo", ti);
			}

			String text = templateEngine.merge("finish-launch-template.vm", email);
			message.setText(text, true);
			message.addInline("logoimg", new UrlResource(getClass().getClassLoader().getResource(LOGO)));
		};
		this.send(preparator);
	}

	/**
	 * Restore password email
	 *
	 * @param subject
	 * @param recipients
	 * @param url
	 */
	public void sendRestorePasswordEmail(final String subject, final String[] recipients, final String url, final String login) {
		MimeMessagePreparator preparator = mimeMessage -> {
			URL logoImg = this.getClass().getClassLoader().getResource(LOGO);
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject(subject);
			message.setTo(recipients);
			message.setFrom(RP_EMAIL);
			Map<String, Object> email = new HashMap<>();
			email.put("login", login);
			email.put("url", url);
			String text = templateEngine.merge("restore-password-template.vm", email);
			message.setText(text, true);
			message.addInline("logoimg", new UrlResource(logoImg));
		};
		this.send(preparator);
	}

	public void setTemplateEngine(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void setAddressFrom(String from) {
		this.addressFrom = from;
	}

	public void sendConfirmationEmail(CreateUserRQFull req, String basicUrl) {
		MimeMessagePreparator preparator = mimeMessage -> {
			URL logoImg = this.getClass().getClassLoader().getResource(LOGO);
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "utf-8");
			message.setSubject("Welcome to Report Portal");
			message.setTo(req.getEmail());
			message.setFrom(RP_EMAIL);
			Map<String, Object> email = new HashMap<>();
			email.put("url", basicUrl);
			email.put("login", normalizeUsername(req.getLogin()));
			email.put("password", req.getPassword());
			String text = templateEngine.merge("create-user-template.vm", email);
			message.setText(text, true);
			message.addInline("logoimg", new UrlResource(logoImg));
		};
		this.send(preparator);
	}
}
