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

package com.epam.ta.reportportal.core.log.impl;

import org.springframework.stereotype.Service;

/**
 * Delete Logs handler. Basic implementation of
 * {@link com.epam.ta.reportportal.core.log.IDeleteLogHandler} interface.
 *
 * @author Henadzi_Vrubleuski
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteLogHandler /*implements IDeleteLogHandler*/ {
	//	private LogRepository logRepository;
	//	private TestItemRepository testItemRepository;
	//	private LaunchRepository launchRepository;
	//	private ProjectRepository projectRepository;
	//	private UserRepository userRepository;
	//
	//	@Autowired
	//	public void setLaunchRepository(LaunchRepository launchRepository) {
	//		this.launchRepository = launchRepository;
	//	}
	//
	//	@Autowired
	//	public void setProjectRepository(ProjectRepository projectRepository) {
	//		this.projectRepository = projectRepository;
	//	}
	//
	//	@Autowired
	//	public void setUserRepository(UserRepository userRepository) {
	//		this.userRepository = userRepository;
	//	}
	//
	//	@Autowired
	//	public void setTestItemRepository(TestItemRepository itemRepository) {
	//		this.testItemRepository = itemRepository;
	//	}
	//
	//	@Autowired
	//	public void setLogRepository(LogRepository logRepository) {
	//		this.logRepository = logRepository;
	//	}
	//
	//	@Override
	//	public OperationCompletionRS deleteLog(String logId, String projectName, String userName) {
	//		User user = userRepository.findOne(userName);
	//		expect(user, notNull()).verify(ErrorType.USER_NOT_FOUND, userName);
	//
	//		Project project = projectRepository.findOne(projectName);
	//		expect(project, notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
	//
	//		Log log = validate(logId, projectName);
	//		validateRoles(log, user, project);
	//		try {
	//			logRepository.delete(log);
	//		} catch (Exception exc) {
	//			throw new ReportPortalException("Error while Log instance deleting.", exc);
	//		}
	//
	//		return new OperationCompletionRS("Log with ID = '" + logId + "' successfully deleted.");
	//	}
	//
	//	/**
	//	 * Validate specified log against parent objects and project
	//	 *
	//	 * @param logId       - validated log ID value
	//	 * @param projectName - specified project name
	//	 * @return Log
	//	 */
	//	private Log validate(String logId, String projectName) {
	//		Log log = logRepository.findOne(logId);
	//		expect(log, notNull()).verify(ErrorType.LOG_NOT_FOUND, logId);
	//
	//		final TestItem testItem = testItemRepository.findOne(log.getTestItemRef());
	//		expect(testItem, not(Preconditions.IN_PROGRESS)).verify(
	//				ErrorType.TEST_ITEM_IS_NOT_FINISHED,
	//				formattedSupplier("Unable to delete log '{}' when test item '{}' in progress state", log.getId(), testItem.getId())
	//		);
	//
	//		final String expectedProjectName = launchRepository.findOne(testItem.getLaunchRef()).getProjectRef();
	//		expect(expectedProjectName, equalTo(projectName)).verify(
	//				ErrorType.FORBIDDEN_OPERATION,
	//				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectName)
	//		);
	//
	//		return log;
	//	}
	//
	//	private void validateRoles(Log log, User user, Project project) {
	//		final TestItem testItem = testItemRepository.findOne(log.getTestItemRef());
	//		final Launch launch = launchRepository.findOne(testItem.getLaunchRef());
	//		if (user.getRole() != ADMINISTRATOR && !user.getId().equalsIgnoreCase(launch.getUserRef())) {
	//			/*
	//			 * Only PROJECT_MANAGER roles could delete launches
	//			 */
	//			UserConfig userConfig = findUserConfigByLogin(project, user.getId());
	//			expect(userConfig, hasProjectRoles(singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
	//		}
	//	}
}