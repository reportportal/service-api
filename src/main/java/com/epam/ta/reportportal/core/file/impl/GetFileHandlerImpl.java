package com.epam.ta.reportportal.core.file.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

/**
 * @author Ivan Budaev
 */
@Service
public class GetFileHandlerImpl implements GetFileHandler {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DataStoreService dataStoreService;

	@Autowired
	private DataEncoder dataEncoder;

	@Override
	public InputStream getUserPhoto(ReportPortalUser loggedInUser) {

		User user = userRepository.findByLogin(loggedInUser.getUsername())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));

		String path = ofNullable(user.getAttachment()).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
				formattedSupplier("User - '{}' does not have a photo.", user.getLogin())
		));
		return userRepository.findUserPhoto(dataEncoder.decode(path)).getInputStream();
	}

	@Override
	public InputStream getUserPhoto(String username, ReportPortalUser loggedInUser) {
		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));

		String path = ofNullable(user.getAttachment()).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
				formattedSupplier("User - '{}' does not have a photo.", user.getLogin())
		));
		return userRepository.findUserPhoto(dataEncoder.decode(path)).getInputStream();
	}

	@Override
	public InputStream loadFileById(String fileId) {
		return dataStoreService.load(fileId);
	}
}
