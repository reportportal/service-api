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

package com.epam.ta.reportportal.core.configs;

import com.epam.reportportal.commons.template.FreemarkerTemplateEngine;
import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.base.Charsets;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * Global Email Configuration<br>
 * Probably will be replaces by configuration per project
 *
 * @author Andrei_Ramanchuk
 */
@Configuration
public class EmailConfiguration {

	@Bean
	public MailServiceFactory initializeEmailServiceFactory(TemplateEngine templateEngine, BasicTextEncryptor encryptor,
			ServerSettingsRepository settingsRepository) {
		return new MailServiceFactory(templateEngine, encryptor, settingsRepository);
	}


	@Bean
	public TemplateEngine getTemplateEngine() {

		Version version = new Version(2, 3, 25);
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(version);

		cfg.setClassForTemplateLoading(EmailConfiguration.class, "/templates/email");

		cfg.setIncompatibleImprovements(version);
		cfg.setDefaultEncoding(Charsets.UTF_8.toString());
		cfg.setLocale(Locale.US);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		return new FreemarkerTemplateEngine(cfg);
	}
}
