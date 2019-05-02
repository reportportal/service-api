/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.PASSED;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class DemoDataLaunchService {

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private String[] platformValues = { "linux", "windows", "macos", "ios", "android", "windows mobile", "ubuntu", "mint", "arch",
			"windows 10", "windows 7", "windows server", "debian", "alpine" };

	@Autowired
	public DemoDataLaunchService(LaunchRepository launchRepository, TestItemRepository testItemRepository) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
	}

	@Transactional
	public Long startLaunch(String name, int i, User user, ReportPortalUser.ProjectDetails projectDetails) {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setMode(Mode.DEFAULT);
		rq.setDescription(ContentUtils.getLaunchDescription());
		rq.setName(name);
		rq.setStartTime(new Date());
		LocalDateTime now = LocalDateTime.now();
		Set<ItemAttributesRQ> attributes = Sets.newHashSet(
				new ItemAttributesRQ("platform", platformValues[new Random().nextInt(platformValues.length)]),
				new ItemAttributesRQ(null, "demo"),
				new ItemAttributesRQ("build", "3." + now.getDayOfMonth() + "." + now.getHour() + "." + i)
		);

		Launch launch = new LaunchBuilder().addStartRQ(rq).addAttributes(attributes).addProject(projectDetails.getProjectId()).get();

		launch.setUser(user);
		launchRepository.save(launch);
		launchRepository.refresh(launch);
		return launch.getId();
	}

	@Transactional
	public void finishLaunch(Long launchId) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));

		if (testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)) {
			testItemRepository.interruptInProgressItems(launchId);
		}

		launch = new LaunchBuilder(launch).addEndTime(new Date()).get();

		StatusEnum fromStatisticsStatus = PASSED;
		if (launchRepository.hasItemsWithStatusNotEqual(launchId, StatusEnum.PASSED)) {
			fromStatisticsStatus = StatusEnum.FAILED;
		}
		launch.setStatus(fromStatisticsStatus);

		launchRepository.save(launch);
	}
}
