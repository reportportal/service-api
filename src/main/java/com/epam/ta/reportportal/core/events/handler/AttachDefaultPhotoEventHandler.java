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

import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.ta.reportportal.util.MultipartFileUtils.getMultipartFile;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Profile("!unittest")
@Component
@Transactional
public class AttachDefaultPhotoEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserCreatedEvent.class);

	private final UserRepository userRepository;

	private final UserBinaryDataService userBinaryDataService;

	@Autowired
	public AttachDefaultPhotoEventHandler(UserRepository userRepository, UserBinaryDataService userBinaryDataService) {
		this.userRepository = userRepository;
		this.userBinaryDataService = userBinaryDataService;
	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		userRepository.findByLogin("superadmin").ifPresent(it -> attachPhoto(it, "image/superAdminPhoto.jpg"));
		userRepository.findByLogin("default").ifPresent(it -> attachPhoto(it, "image/defaultUserPhoto.jpg"));
	}

	private void attachPhoto(User user, String photoPath) {
		if (StringUtils.isEmpty(user.getAttachment())) {
			try {
				userBinaryDataService.saveUserPhoto(user, getMultipartFile(photoPath));
			} catch (Exception exception) {
				LOGGER.error(Suppliers.formattedSupplier("Cannot attach default photo to user '{}'.", user.getLogin()).get(), exception);
			}
		}
	}
}
