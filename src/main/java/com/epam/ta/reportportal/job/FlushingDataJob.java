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

import com.epam.ta.reportportal.commons.Constants;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.demo_data.DemoDataRq;
import com.epam.ta.reportportal.demo_data.DemoDataService;
import com.epam.ta.reportportal.events.handler.AddDemoProjectEventHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("demo")
public class FlushingDataJob implements Job {

	@Value("${rp.demo.data}")
	private Integer launchesCount;

	@Autowired
	private UserCreationBidRepository creationBidRepository;

	@Autowired
	private RestorePasswordBidRepository passwordBidRepository;

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
	private DemoDataService demoDataService;

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
		List<String> projectNames = projectRepository.findAllProjectNames()
				.stream()
				.filter(it -> !it.startsWith(Constants.DEFAULT_ADMIN.toString() + "_personal"))
				.collect(Collectors.toList());
		projectRepository.delete(projectNames);
		projectNames.forEach(name -> logIndexer.deleteIndex(name));
		List<String> users = userRepository.findAll()
				.stream()
				.map(User::getLogin)
				.filter(it -> !it.equalsIgnoreCase(Constants.DEFAULT_ADMIN.toString()))
				.collect(Collectors.toList());
		userRepository.delete(users);
		preferenceRepository.deleteAll();
		dataStorage.deleteAll();

		User superadmin = userRepository.findOne(Constants.DEFAULT_ADMIN.toString());
		String photoId = userRepository.uploadUserPhoto(superadmin.getLogin(),
				new BinaryData(MediaType.IMAGE_JPEG_VALUE,
						null,
						FlushingDataJob.class.getClassLoader().getResourceAsStream("superAdminPhoto.jpg")
				)
		);
		superadmin.setPhotoId(photoId);
		userRepository.save(superadmin);

		addDemoProjectEventHandler.addDefaultUser();
		DemoDataRq demoDataRq = new DemoDataRq();
		demoDataRq.setPostfix("Demo");
		demoDataRq.setCreateDashboard(true);
		demoDataRq.setLaunchesQuantity(launchesCount);
		demoDataService.generate(demoDataRq, Constants.DEFAULT_USER.toString() + "_" + "personal", Constants.DEFAULT_USER.toString());
		LOGGER.info("Finish flushing all existing data!");
	}
}
