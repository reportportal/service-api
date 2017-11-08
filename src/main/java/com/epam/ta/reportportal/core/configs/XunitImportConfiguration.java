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
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.imprt.impl.junit.XunitImportHandler;
import com.epam.ta.reportportal.core.imprt.impl.junit.XunitParseJob;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Configs for beans that are related to
 * importing xml report for junit tests
 * and SAX-parsing handler
 *
 * @author Pavel_Bortnik
 */
@Configuration
public class XunitImportConfiguration {

	@Bean(name = "junitParseJob")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public XunitParseJob junitParseJob() {
		return new XunitParseJob();
	}

	@Bean(name = "junitImportHandler")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public XunitImportHandler junitImportHandler() {
		return new XunitImportHandler();
	}
}
