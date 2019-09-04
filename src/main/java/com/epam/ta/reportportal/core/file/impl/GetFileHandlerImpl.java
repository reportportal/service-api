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
package com.epam.ta.reportportal.core.file.impl;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetFileHandlerImpl implements GetFileHandler {

	private final UserRepository userRepository;

	private final DataStoreService dataStoreService;

	@Autowired
	public GetFileHandlerImpl(UserRepository userRepository, DataStoreService dataStoreService) {
		this.userRepository = userRepository;
		this.dataStoreService = dataStoreService;
	}

	@Override
	public BinaryData getUserPhoto(ReportPortalUser loggedInUser) {
		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
		return dataStoreService.loadUserPhoto(user);
	}

	@Override
	public BinaryData getUserPhoto(String username, ReportPortalUser loggedInUser, ReportPortalUser.ProjectDetails projectDetails) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
		expect(
				ProjectUtils.isAssignedToProject(user, projectDetails.getProjectId()),
				Predicate.isEqual(true)
		).verify(ErrorType.ACCESS_DENIED, formattedSupplier("You are not assigned to project '{}'", projectDetails.getProjectName()));
		return dataStoreService.loadUserPhoto(user);
	}

	@Override
	public BinaryData loadFileById(String fileId, ReportPortalUser.ProjectDetails projectDetails) {
		return dataStoreService.loadFile(fileId, projectDetails);
	}
}
