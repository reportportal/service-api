/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.BinaryData;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.core.user.event.UpdateUserRoleEvent;
import com.epam.ta.reportportal.core.user.event.UpdatedRole;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ImageFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
import com.google.common.base.Charsets;
import com.sun.javafx.binding.StringFormatter;
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

/**
 * Edit user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
public class EditUserHandlerImpl implements EditUserHandler {

	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private DataStore dataStore;

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, UserRole userRole) {
		return editUser(username, editUserRQ, userRole == ADMINISTRATOR);
	}

	@Override
	public OperationCompletionRS uploadPhoto(String username, MultipartFile file) {
		try {
			validatePhoto(file);
			BinaryData binaryData = new BinaryData(file.getContentType(), file.getSize(), file.getInputStream());
			userRepository.replaceUserPhoto(username, binaryData);
		} catch (IOException e) {
			fail().withError(BINARY_DATA_CANNOT_BE_SAVED);
		}
		return new OperationCompletionRS("Profile photo has been uploaded successfully");
	}

	@Override
	public OperationCompletionRS deletePhoto(String login) {
		User user = userRepository.findByLogin(login).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, login));
		expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change photo for external user");
		if (null != user.getAttachment()) {
			dataStore.delete(user.getAttachment());
		}
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
		expect(user, notNull()).verify(USER_NOT_FOUND, username);
		boolean isRoleChanged = false;
		UpdatedRole source = null;

		if ((null != editUserRQ.getRole()) && isAdmin) {
			Optional<UserRole> newRole = UserRole.findByName(editUserRQ.getRole());
			expect(newRole.isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter.");
			//noinspection ConstantConditions
			user.setRole(newRole.get());
			//noinspection ConstantConditions
			source = new UpdatedRole(username, newRole.get());
			isRoleChanged = true;
		}

		if (null != editUserRQ.getDefaultProject()) {
			Project defaultProject = projectRepository.findByName(editUserRQ.getDefaultProject().toLowerCase())
					.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, editUserRQ.getDefaultProject()));
			//TODO check if user is owner
			user.setDefaultProject(defaultProject);
		}

		if (null != editUserRQ.getEmail()) {
			String updEmail = editUserRQ.getEmail().toLowerCase().trim();
			expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change email for external user");
			expect(UserUtils.isEmailValid(updEmail), equalTo(true)).verify(BAD_REQUEST_ERROR, " wrong email: " + updEmail);
			User byEmail = userRepository.findByEmail(updEmail)
					.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND,
							StringFormatter.format("User with email - {} was not found", updEmail)
					));

			expect(username, equalTo(byEmail.getLogin())).verify(USER_ALREADY_EXISTS, updEmail);

			expect(UserUtils.isEmailValid(updEmail), equalTo(true)).verify(BAD_REQUEST_ERROR, updEmail);

			List<Project> userProjects = projectRepository.findUserProjects(username);
			userProjects.forEach(project -> ProjectUtils.updateProjectRecipients(user.getEmail(), updEmail, project));
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
