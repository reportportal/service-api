/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.BinaryData;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.core.user.event.UpdateUserRoleEvent;
import com.epam.ta.reportportal.core.user.event.UpdatedRole;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ImageFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.util.integration.email.EmailIntegrationService;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
import com.google.common.base.Charsets;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.core.user.impl.CreateUserHandlerImpl.HASH_FUNCTION;
import static com.epam.ta.reportportal.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.entity.user.UserType.INTERNAL;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.*;
import static java.util.Optional.ofNullable;

/**
 * Edit user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
public class EditUserHandlerImpl implements EditUserHandler {

	private final UserRepository userRepository;

	private final ProjectRepository projectRepository;

	private final ApplicationEventPublisher eventPublisher;

	private final DataStoreService dataStoreService;

	private final DataEncoder dataEncoder;

	private final EmailIntegrationService emailIntegrationService;

	@Autowired
	public EditUserHandlerImpl(UserRepository userRepository, ProjectRepository projectRepository, ApplicationEventPublisher eventPublisher,
			DataStoreService dataStoreService, DataEncoder dataEncoder, EmailIntegrationService emailIntegrationService) {
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.eventPublisher = eventPublisher;
		this.dataStoreService = dataStoreService;
		this.dataEncoder = dataEncoder;
		this.emailIntegrationService = emailIntegrationService;
	}

	@Override
	public OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, UserRole userRole) {
		return editUser(username, editUserRQ, userRole == ADMINISTRATOR);
	}

	@Override
	public OperationCompletionRS uploadPhoto(String username, MultipartFile file) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
		try {
			validatePhoto(file);
			BinaryData binaryData = new BinaryData(file.getContentType(), file.getSize(), file.getInputStream());
			String path = userRepository.replaceUserPhoto(username, binaryData);
			user.setAttachment(dataEncoder.encode(path));
		} catch (IOException e) {
			fail().withError(BINARY_DATA_CANNOT_BE_SAVED);
		}
		return new OperationCompletionRS("Profile photo has been uploaded successfully");
	}

	@Override
	public OperationCompletionRS deletePhoto(String login) {
		User user = userRepository.findByLogin(login).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, login));
		expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change photo for external user");
		ofNullable(user.getAttachment()).ifPresent(attachment -> {
			dataStoreService.delete(attachment);
			user.setAttachment(null);
			user.setAttachmentThumbnail(null);
		});
		return new OperationCompletionRS("Profile photo has been deleted successfully");
	}

	@Override
	public OperationCompletionRS changePassword(ReportPortalUser loggedInUser, ChangePasswordRQ changePasswordRQ) {
		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
		expect(user.getUserType(), equalTo(INTERNAL)).verify(FORBIDDEN_OPERATION, "Impossible to change password for external users.");

		expect(user.getPassword(), equalTo(HASH_FUNCTION.hashString(changePasswordRQ.getOldPassword(), Charsets.UTF_8).toString())).verify(FORBIDDEN_OPERATION,
				"Old password not match with stored."
		);
		user.setPassword(HASH_FUNCTION.hashString(changePasswordRQ.getNewPassword(), Charsets.UTF_8).toString());
		userRepository.save(user);
		return new OperationCompletionRS("Password has been changed successfully");
	}

	private OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, boolean isAdmin) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
		boolean isRoleChanged = false;
		UpdatedRole source = null;

		if ((null != editUserRQ.getRole()) && isAdmin) {
			UserRole newRole = UserRole.findByName(editUserRQ.getRole())
					.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter."));
			//noinspection ConstantConditions
			user.setRole(newRole);
			//noinspection ConstantConditions
			source = new UpdatedRole(username, newRole);
			isRoleChanged = true;
		}

		if (null != editUserRQ.getDefaultProject()) {
			Project defaultProject = projectRepository.findByName(editUserRQ.getDefaultProject().toLowerCase())
					.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, editUserRQ.getDefaultProject()));
			//TODO check if user is owner
			user.setDefaultProject(defaultProject);
		}

		if (null != editUserRQ.getEmail() && !editUserRQ.getEmail().equals(user.getEmail())) {
			String updEmail = editUserRQ.getEmail().toLowerCase().trim();
			expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change email for external user");
			expect(UserUtils.isEmailValid(updEmail), equalTo(true)).verify(BAD_REQUEST_ERROR, " wrong email: " + updEmail);
			final Optional<User> byEmail = userRepository.findByEmail(updEmail);

			expect(byEmail, Predicates.not(Optional::isPresent)).verify(USER_ALREADY_EXISTS, updEmail);

			List<Project> userProjects = projectRepository.findUserProjects(username);
			userProjects.forEach(project -> emailIntegrationService.updateProjectRecipients(user.getEmail(), updEmail, project));
			user.setEmail(updEmail);
			try {
				projectRepository.saveAll(userProjects);
			} catch (Exception exp) {
				throw new ReportPortalException("PROJECT update exception while USER editing.", exp);
			}
		}

		if (null != editUserRQ.getFullName()) {
			expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change full name for external user");
			user.setFullName(editUserRQ.getFullName());
		}

		try {
			userRepository.save(user);
			if (isRoleChanged) {
				eventPublisher.publishEvent(new UpdateUserRoleEvent(source));
			}
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User editing.", exp);
		}

		return new OperationCompletionRS("User with login = '" + user.getLogin() + "' successfully updated");
	}

	private void validatePhoto(MultipartFile file) throws IOException {
		expect(file.getSize() < MAX_PHOTO_SIZE, equalTo(true)).verify(BINARY_DATA_CANNOT_BE_SAVED, "Image size should be less than 1 mb");
		MediaType mediaType = new AutoDetectParser().getDetector().detect(TikaInputStream.get(file.getBytes()), new Metadata());
		String subtype = mediaType.getSubtype();
		expect(ImageFormat.fromValue(subtype), notNull()).verify(BINARY_DATA_CANNOT_BE_SAVED,
				"Image format should be " + ImageFormat.getValues()
		);
		BufferedImage read = ImageIO.read(file.getInputStream());
		expect((read.getHeight() <= MAX_PHOTO_HEIGHT) && (read.getWidth() <= MAX_PHOTO_WIDTH), equalTo(true)).verify(BINARY_DATA_CANNOT_BE_SAVED,
				"Image size should be 300x500px or less"
		);
	}
}
