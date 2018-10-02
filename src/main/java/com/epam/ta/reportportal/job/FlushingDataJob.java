/*
 * Copyright 2018 EPAM Systems
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
package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.events.handler.AddDemoProjectEventHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("demo")
public class FlushingDataJob implements Job {

	@Autowired
	private UserCreationBidRepository creationBidRepository;

	@Autowired
	private RestorePasswordBidRepository passwordBidRepository;

	@Autowired
	private ServerSettingsRepository serverSettingsRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserPreferenceRepository preferenceRepository;

	@Autowired
	private ILogIndexer logIndexer;

	@Autowired
	private AddDemoProjectEventHandler addDemoProjectEventHandler;

	@Autowired
	private DataStorage dataStorage;

	@Autowired
	private MongoOperations mongoOperations;

	private static final Logger LOGGER = LoggerFactory.getLogger(FlushingDataJob.class);

	@Override
	public void execute(JobExecutionContext context) {
		LOGGER.info("Start flushing all existing data!");

		mongoOperations.dropCollection("launchMetaInfo");
		mongoOperations.dropCollection("oauth_access_token");
		mongoOperations.dropCollection("oauth_refresh_token");
		mongoOperations.dropCollection("sessions");

		passwordBidRepository.deleteAll();
		creationBidRepository.deleteAll();

		List<String> allProjectNames = projectRepository.findAllProjectNames();
		projectRepository.delete(allProjectNames);
		allProjectNames.forEach(name -> logIndexer.deleteIndex(name));
		serverSettingsRepository.deleteAll();
		userRepository.deleteAll();
		preferenceRepository.deleteAll();
		dataStorage.deleteAll();
		addDemoProjectEventHandler.onApplicationEvent(null);
		LOGGER.info("Finish flushing all existing data!");
	}
}
