/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.imprt.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public abstract class AbstractImportStrategy implements ImportStrategy {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractImportStrategy.class);
	private static final Date initialStartTime = new Date(0);
	protected static final ExecutorService service = Executors.newFixedThreadPool(5);

	private StartLaunchHandler startLaunchHandler;

	private FinishLaunchHandler finishLaunchHandler;

	private LaunchRepository launchRepository;

	@Autowired
	public void setStartLaunchHandler(StartLaunchHandler startLaunchHandler) {
		this.startLaunchHandler = startLaunchHandler;
	}

	@Autowired
	public void setFinishLaunchHandler(FinishLaunchHandler finishLaunchHandler) {
		this.finishLaunchHandler = finishLaunchHandler;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	protected ParseResults processResults(CompletableFuture... futures) {
		ParseResults results = new ParseResults();
		Arrays.stream(futures).map(it -> (ParseResults) it.join()).forEach(res -> {
			results.checkAndSetStartLaunchTime(res.getStartTime());
			results.increaseDuration(res.getDuration());
		});
		return results;
	}

	@Transactional
	protected Long startLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, String launchName) {
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setStartTime(initialStartTime);
		startLaunchRQ.setName(launchName);
		startLaunchRQ.setMode(Mode.DEFAULT);
		return startLaunchHandler.startLaunch(user, projectDetails, startLaunchRQ).getId();
	}

	@Transactional
	protected void finishLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			ParseResults results) {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(results.getEndTime());
		finishLaunchHandler.finishLaunch(launchId, finishExecutionRQ, projectDetails, user);
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		launch.setStartTime(results.getStartTime());
		launchRepository.save(launch);
	}

	/**
	 * Got a cause exception message if it has any.
	 *
	 * @param e Exception
	 * @return Clean exception message
	 */
	protected String cleanMessage(Exception e) {
		if (e.getCause() != null) {
			return e.getCause().getMessage();
		}
		return e.getMessage();
	}

	/*
	 * if the importing results do not contain initial timestamp a launch gets
	 * a default date if the launch is broken, time should be updated to not to broke
	 * the statistics
	 */
	protected void updateBrokenLaunch(Long savedLaunchId) {
		if (savedLaunchId != null) {
			Launch launch = launchRepository.findById(savedLaunchId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND));
			launch.setStartTime(LocalDateTime.now());
			launch.setStatus(StatusEnum.INTERRUPTED);
			launchRepository.save(launch);
		}
	}
}
