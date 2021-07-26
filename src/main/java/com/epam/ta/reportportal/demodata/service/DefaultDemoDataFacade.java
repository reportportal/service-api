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
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.demodata.model.DemoLaunch;
import com.epam.ta.reportportal.demodata.model.RootMetaData;
import com.epam.ta.reportportal.demodata.service.generator.SuiteGenerator;
import com.epam.ta.reportportal.demodata.service.generator.SuiteGeneratorResolver;
import com.epam.ta.reportportal.demodata.service.generator.model.SuiteGeneratorType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.demodata.service.Constants.NAME;
import static java.util.stream.Collectors.toList;

/**
 * @author Ihar Kahadouski
 */
@Service
public class DefaultDemoDataFacade implements DemoDataFacade {

	public static final int LAUNCH_LOGS_COUNT = 10;

	private final ObjectMapper objectMapper;

	private final DemoDataLaunchService demoDataLaunchService;
	private final DemoLogsService demoLogsService;
	private final SuiteGeneratorResolver suiteGeneratorResolver;

	private final TaskExecutor executor;

	private final UserRepository userRepository;

	@Value("classpath:demo/launch/")
	private String resourceFolder;

	@Value("${rp.environment.variable.demo.source}")
	private String[] sources;

	public DefaultDemoDataFacade(DemoDataLaunchService demoDataLaunchService, DemoLogsService demoLogsService, ObjectMapper objectMapper,
			SuiteGeneratorResolver suiteGeneratorResolver, UserRepository userRepository,
			@Qualifier("demoDataTaskExecutor") TaskExecutor executor) {
		this.demoDataLaunchService = demoDataLaunchService;
		this.suiteGeneratorResolver = suiteGeneratorResolver;
		this.demoLogsService = demoLogsService;
		this.objectMapper = objectMapper;
		this.userRepository = userRepository;
		this.executor = executor;
	}

	@Override
	public List<Long> generateDemoLaunches(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		return CompletableFuture.supplyAsync(() -> Stream.of(sources).map(source -> resourceFolder + source).map(source -> {
			try {
				final DemoLaunch demoLaunch = objectMapper.readValue(ResourceUtils.getURL(source), new TypeReference<DemoLaunch>() {
				});
				return generateLaunch(demoLaunch, user, projectDetails);
			} catch (IOException e) {
				throw new ReportPortalException("Unable to load suites description. " + e.getMessage(), e);
			}
		}).collect(toList()), executor).join();
	}

	private Long generateLaunch(DemoLaunch demoLaunch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {

		final User creator = userRepository.findById(user.getUserId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUsername()));

		final Launch launch = demoDataLaunchService.startLaunch(NAME, creator, projectDetails);

		demoLaunch.getSuites().forEach(suite -> {
			final SuiteGeneratorType suiteGeneratorType = SuiteGeneratorType.valueOf(suite.getType());
			final SuiteGenerator suiteGenerator = suiteGeneratorResolver.resolve(suiteGeneratorType);
			suiteGenerator.generateSuites(suite, RootMetaData.of(launch.getUuid(), user, projectDetails));
		});

		demoDataLaunchService.finishLaunch(launch.getUuid());
		final List<Log> logs = demoLogsService.generateLaunchLogs(LAUNCH_LOGS_COUNT, launch.getUuid(), launch.getStatus());
		demoLogsService.attachFiles(logs, projectDetails.getProjectId(), launch.getUuid());
		return launch.getId();
	}
}
