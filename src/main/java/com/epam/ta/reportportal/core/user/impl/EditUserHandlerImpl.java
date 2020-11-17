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

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ImageFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
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

	private final UserBinaryDataService userBinaryDataService;

	private final PasswordEncoder passwordEncoder;

	private final MimetypesFileTypeMap mimetypesFileTypeMap;

	private final AutoDetectParser autoDetectParser;

	@Autowired
	public EditUserHandlerImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, ProjectRepository projectRepository,
			UserBinaryDataService userBinaryDataService, MimetypesFileTypeMap mimetypesFileTypeMap, AutoDetectParser autoDetectParser) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.userBinaryDataService = userBinaryDataService;
		this.mimetypesFileTypeMap = mimetypesFileTypeMap;
		this.autoDetectParser = autoDetectParser;
	}

	@Override
	public OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, ReportPortalUser editor) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));

		if (null != editUserRQ.getRole()) {

			BusinessRule.expect(editor.getUserRole(), equalTo(UserRole.ADMINISTRATOR))
					.verify(ACCESS_DENIED, "Current Account Role can't update roles.");

			BusinessRule.expect(user, u -> !u.getLogin().equalsIgnoreCase(editor.getUsername()))
					.verify(ErrorType.ACCESS_DENIED, "You cannot update your role.");

			UserRole newRole = UserRole.findByName(editUserRQ.getRole())
					.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Incorrect specified Account Role parameter."));
			user.setRole(newRole);
		}

		if (null != editUserRQ.getEmail() && !editUserRQ.getEmail().equals(user.getEmail())) {
			String updEmail = editUserRQ.getEmail().toLowerCase().trim();
			expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change email for external user");
			expect(UserUtils.isEmailValid(updEmail), equalTo(true)).verify(BAD_REQUEST_ERROR, " wrong email: " + updEmail);
			final Optional<User> byEmail = userRepository.findByEmail(updEmail);

			expect(byEmail, Predicates.not(Optional::isPresent)).verify(USER_ALREADY_EXISTS, updEmail);

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
		} catch (Exception exp) {
			throw new ReportPortalException("Error while User editing.", exp);
		}

		return new OperationCompletionRS("User with login = '" + user.getLogin() + "' successfully updated");
	}

	@Override
	public OperationCompletionRS uploadPhoto(String username, MultipartFile file) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
		validatePhoto(file);
		userBinaryDataService.saveUserPhoto(user, file);
		return new OperationCompletionRS("Profile photo has been uploaded successfully");
	}

	@Override
	public OperationCompletionRS deletePhoto(String login) {
		User user = userRepository.findByLogin(login).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, login));
		expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED, "Unable to change photo for external user");
		userBinaryDataService.deleteUserPhoto(user);
		return new OperationCompletionRS("Profile photo has been deleted successfully");
	}

	@Override
	public OperationCompletionRS changePassword(ReportPortalUser loggedInUser, ChangePasswordRQ request) {
		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
		expect(user.getUserType(), equalTo(INTERNAL)).verify(FORBIDDEN_OPERATION, "Impossible to change password for external users.");
		expect(passwordEncoder.matches(request.getOldPassword(), user.getPassword()), Predicate.isEqual(true)).verify(FORBIDDEN_OPERATION,
				"Old password not match with stored."
		);
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		return new OperationCompletionRS("Password has been changed successfully");
	}

	private void validatePhoto(MultipartFile file) {
		expect(file.getSize() < MAX_PHOTO_SIZE, equalTo(true)).verify(BINARY_DATA_CANNOT_BE_SAVED, "Image size should be less than 1 mb");

		final MediaType mediaType = resolveMediaType(file);
		try (final InputStream inputStream = file.getInputStream()) {
			Dimension dimension = getImageDimension(mediaType, inputStream).orElseThrow(() -> new ReportPortalException(
					BINARY_DATA_CANNOT_BE_SAVED,
					"Unable to resolve image size"
			));
			expect((dimension.getHeight() <= MAX_PHOTO_HEIGHT) && (dimension.getWidth() <= MAX_PHOTO_WIDTH), equalTo(true)).verify(
					BINARY_DATA_CANNOT_BE_SAVED,
					"Image size should be 300x500px or less"
			);
		} catch (IOException e) {
			fail().withError(BINARY_DATA_CANNOT_BE_SAVED);
		}
	}

	private MediaType resolveMediaType(MultipartFile file) {
		return ofNullable(file.getContentType()).flatMap(string -> ofNullable(MediaType.parse(string)).filter(mediaType -> ImageFormat.fromValue(
				mediaType.getSubtype()).isPresent()))
						.orElseGet(() -> {
							try (final TikaInputStream tikaInputStream = TikaInputStream.get(file.getInputStream())) {
								MediaType mediaType = autoDetectParser.getDetector().detect(tikaInputStream, new Metadata());
								expect(ImageFormat.fromValue(mediaType.getSubtype()), Optional::isPresent).verify(
										BINARY_DATA_CANNOT_BE_SAVED,
										"Image format should be " + ImageFormat.getValues()
								);
								return mediaType;
							} catch (IOException e) {
								throw new ReportPortalException(BINARY_DATA_CANNOT_BE_SAVED);
							}
						});
	}

	private Optional<Dimension> getImageDimension(MediaType mediaType, InputStream inputStream) {
		for (Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(String.valueOf(mediaType)); iterator.hasNext(); ) {
			ImageReader reader = iterator.next();
			try (ImageInputStream stream = ImageIO.createImageInputStream(inputStream)) {
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				return Optional.of(new Dimension(width, height));
			} catch (IOException e) {
				//Try next ImageReader
			} finally {
				reader.dispose();
			}
		}
		return Optional.empty();
	}
}
