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

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
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

	private final UserBinaryDataService userDataStoreService;

	private final AttachmentBinaryDataService attachmentBinaryDataService;

	private final ProjectExtractor projectExtractor;

	@Autowired
	public GetFileHandlerImpl(UserRepository userRepository, UserBinaryDataService userDataStoreService,
			AttachmentBinaryDataService attachmentBinaryDataService, ProjectExtractor projectExtractor) {
		this.userRepository = userRepository;
		this.userDataStoreService = userDataStoreService;
		this.attachmentBinaryDataService = attachmentBinaryDataService;
		this.projectExtractor = projectExtractor;
	}

	@Override
	public BinaryData getUserPhoto(ReportPortalUser loggedInUser, boolean loadThumbnail) {
		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
		return userDataStoreService.loadUserPhoto(user, loadThumbnail);
	}

	@Override
	public BinaryData getUserPhoto(String username, ReportPortalUser loggedInUser, String organizationSlug,
			String projectKey, boolean loadThumbnail) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
		ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetailsAdmin(loggedInUser, organizationSlug, projectKey);
		if (loggedInUser.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(
					ProjectUtils.isAssignedToProject(user, projectDetails.getProjectId()),
					Predicate.isEqual(true)
			).verify(ErrorType.ACCESS_DENIED, formattedSupplier("You are not assigned to project '{}'", projectDetails.getProjectName()));
		}
		return userDataStoreService.loadUserPhoto(user, loadThumbnail);
	}

	@Override
	public BinaryData loadFileById(Long fileId, ReportPortalUser.ProjectDetails projectDetails) {
		return attachmentBinaryDataService.load(fileId, projectDetails);
	}
}
