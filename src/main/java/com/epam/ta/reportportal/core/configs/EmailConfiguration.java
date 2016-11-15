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
import com.epam.reportportal.commons.template.VelocityTemplateEngine;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

/**
 * Global Email Configuration<br>
 * Probably will be replaces by configuration per project
 * 
 * @author Andrei_Ramanchuk
 */
@Configuration
public class EmailConfiguration {

	@Bean
	public MailServiceFactory initializeEmailServiceFactory() {
		return new MailServiceFactory();
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
