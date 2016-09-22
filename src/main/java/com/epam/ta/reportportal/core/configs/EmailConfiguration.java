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

package com.epam.ta.reportportal.core.configs;
import java.util.Properties;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.reportportal.commons.template.VelocityTemplateEngine;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;

/**
 * Global Email Configuration<br>
 * Probably will be replaces by configuration per project
 * 
 * @author Andrei_Ramanchuk
 */
@Configuration
public class EmailConfiguration {

	@Autowired
	private ServerSettingsRepository settingsRepository;

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	@Autowired
	@Value("${rp.email.server}")
	private String defaultEmailServer;

	@Autowired
	@Value("${rp.email.port}")
	private Integer defaultEmailPort;

	@Autowired
	@Value("${rp.email.protocol}")
	private String defaultEmailProtocol;

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public EmailService initializeEmailService() {
		if (null != settingsRepository.findOne("default")) {
			ServerEmailConfig config = settingsRepository.findOne("default").getServerEmailConfig();

			Properties javaMailProperties = new Properties();
			javaMailProperties.put("mail.smtp.connectiontimeout", 5000);
			javaMailProperties.put("mail.smtp.auth", config.getAuthEnabled());
			javaMailProperties.put("mail.debug", config.getDebug());

			EmailService emailService = new EmailService(javaMailProperties);
			emailService.setTemplateEngine(getVelocityTemplateEngine());
			emailService.setHost(config.getHost());
			emailService.setPort(config.getPort());
			emailService.setProtocol(config.getProtocol());
			if (config.getAuthEnabled()) {
				emailService.setUsername(config.getUsername());
				emailService.setPassword(simpleEncryptor.decrypt(config.getPassword()));
			}
			return emailService;
		} else {
			Properties javaMailProperties = new Properties();
			javaMailProperties.put("mail.smtp.connectiontimeout", 5000);
			javaMailProperties.put("mail.smtp.auth", false);
			javaMailProperties.put("mail.debug", false);

			EmailService emailService = new EmailService(javaMailProperties);
			emailService.setTemplateEngine(getVelocityTemplateEngine());
			emailService.setHost(defaultEmailServer);
			emailService.setPort(defaultEmailPort);
			emailService.setProtocol(defaultEmailProtocol);
			return emailService;
		}
	}

	@Bean(name = "velocityEngine")
	public VelocityEngineFactoryBean getVelocityEngineFactory() {
		VelocityEngineFactoryBean velocityEngineFactory = new VelocityEngineFactoryBean();
		velocityEngineFactory.setResourceLoaderPath("classpath:/templates/email");
		velocityEngineFactory.setPreferFileSystemAccess(false);
		return velocityEngineFactory;
	}

	@Bean(name = "templateVelocityEngine")
	public VelocityTemplateEngine getVelocityTemplateEngine() {
		return new VelocityTemplateEngine(getVelocityEngineFactory().getObject());
	}
}