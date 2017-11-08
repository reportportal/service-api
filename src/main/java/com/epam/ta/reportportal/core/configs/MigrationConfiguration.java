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

import com.epam.ta.reportportal.commons.accessible.Accessible;
import com.epam.ta.reportportal.config.MongodbConfiguration;
import com.github.mongobee.Mongobee;
import com.github.mongobee.dao.ChangeEntryDao;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Mongo Migration tool configs
 *
 * @author Andrei Varabyeu
 */
@Configuration
public class MigrationConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbConfiguration.class);

	@Autowired
	private MongodbConfiguration.MongoProperties mongoProperties;
	@Autowired
	private Environment environment;

	@Bean
	@Autowired
	@Profile({ "!unittest" })
	public Mongobee mongobee(MongoClient mongoClient) {
		Mongobee runner = new Mongobee(mongoClient);
		runner.setDbName(mongoProperties.getDatabase());
		runner.setChangeLogsScanPackage("com.epam.ta.reportportal.migration");
		runner.setSpringEnvironment(environment);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				LOGGER.info("Making sure mongobee lock is removed...");
				ChangeEntryDao dao = (ChangeEntryDao) Accessible.on(runner).field(Mongobee.class.getDeclaredField("dao")).getValue();
				if (dao.isProccessLockHeld()) {
					LOGGER.warn("Mongobee lock is NOT removed. Removing...");
					dao.releaseProcessLock();
				}
				LOGGER.info("Mongobee lock has been removed");
			} catch (Exception ignored) {
				LOGGER.error("Cannot make sure mongobee lock has been removed", ignored);
			}
		}));
		return runner;
	}
}
