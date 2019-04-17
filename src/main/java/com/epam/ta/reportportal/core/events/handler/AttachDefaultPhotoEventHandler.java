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

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.BinaryData;
import com.epam.ta.reportportal.core.events.AttachDefaultPhotoEvent;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.InputStream;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Profile("!unittest")
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class AttachDefaultPhotoEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserCreatedEvent.class);

	private static final String DEFAULT_PHOTO_NAME = "default_reportportal_user_photo";

	private final UserRepository userRepository;

	private final DataStore dataStore;

	private final DataEncoder encoder;

	@Autowired
	public AttachDefaultPhotoEventHandler(UserRepository userRepository, DataStore dataStore, DataEncoder encoder) {
		this.userRepository = userRepository;
		this.dataStore = dataStore;
		this.encoder = encoder;
	}

	@TransactionalEventListener
	public void handleDefaultPhotoAttached(AttachDefaultPhotoEvent event) {
		final User user = userRepository.findById(event.getUserId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, event.getUserId()));

		if (null == user.getAttachment()) {
			if (dataStore.exists(DEFAULT_PHOTO_NAME)) {
				user.setAttachment(encoder.encode(DEFAULT_PHOTO_NAME));
			} else {
				user.setAttachment(savePhoto(DEFAULT_PHOTO_NAME, "image/nonameUserPhoto.jpg"));
			}
		}

	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		userRepository.findByLogin("superadmin").ifPresent(it -> attachPhoto(it, "image/superAdminPhoto.jpg"));
		userRepository.findByLogin("default").ifPresent(it -> attachPhoto(it, "image/defaultUserPhoto.jpg"));
	}

	private void attachPhoto(User user, String photoPath) {
		if (null == user.getAttachment()) {
			user.setAttachment(savePhoto(user.getLogin(), photoPath));
		}
	}

	private String savePhoto(String username, String photoPath) {
		final InputStream inputStream;
		try {
			inputStream = new ClassPathResource(photoPath).getInputStream();
			BinaryData binaryData = new BinaryData(MediaType.IMAGE_JPEG_VALUE, (long) inputStream.available(), inputStream);
			return encoder.encode(userRepository.replaceUserPhoto(username, binaryData));

		} catch (Exception exception) {
			LOGGER.error("Cannot attach default photo to user {}. Error: {}", username, exception);
		}
		return null;
	}
}
