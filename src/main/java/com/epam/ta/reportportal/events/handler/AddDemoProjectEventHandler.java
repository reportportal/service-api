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

package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.commons.Constants;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.settings.AnalyticsDetails;
import com.epam.ta.reportportal.database.entity.settings.ServerEmailDetails;
import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.database.personal.PersonalProjectService;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Initial deploy data
 *
 * @author Andrei_Varabyeu
 * @author Andrei_Ramanchuk
 */
@Component
public class AddDemoProjectEventHandler implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddDemoProjectEventHandler.class);
	private static final String DEFAULT_PROFILE_ID = "default";

	@Value("${rp.email.server}")
	private String host;

	@Value("${rp.email.port}")
	private int port;

	@Value("${rp.email.protocol}")
	private String protocol;

	@Value("${rp.email.account}")
	private String username;

	@Value("${rp.email.password}")
	private String password;

	@Value("${rp.email.from}")
	private String addressFrom;

	@Value("${rp.email.auth:false}")
	private boolean isEnable;

	@Value("${rp.email.debug:false}")
	private boolean isDebug;

	@Value("${rp.analytics.enableByDefault:true}")
	private boolean enableByDefault;

	/**
	 * Photo avatar for non-existing users
	 */
	public static final Supplier<BinaryData> NONAME_USER_PHOTO = Suppliers.memoize(() -> new BinaryData(MediaType.IMAGE_JPEG_VALUE, null,
			AddDemoProjectEventHandler.class.getClassLoader().getResourceAsStream("nonameUserPhoto.jpg")
	));

	/**
	 * Default user avatar
	 */
	public static final Supplier<BinaryData> DEMO_USER_PHOTO = Suppliers.memoize(() -> new BinaryData(MediaType.IMAGE_JPEG_VALUE, null,
			AddDemoProjectEventHandler.class.getClassLoader().getResourceAsStream("defaultUserPhoto.jpg")
	));

	/**
	 * Administrator avatar
	 */
	public static final Supplier<BinaryData> DEFAULT_ADMIN_PHOTO = Suppliers.memoize(() -> new BinaryData(MediaType.IMAGE_JPEG_VALUE, null,
			AddDemoProjectEventHandler.class.getClassLoader().getResourceAsStream("superAdminPhoto.jpg")
	));

	public static final Supplier<User> DEFAULT_ADMIN = Suppliers.memoize(() -> {
		LOGGER.info("========== DEFAULT ADMINISTRATOR CREATION ==========");
		User user = new User();
		user.setLogin(Constants.DEFAULT_ADMIN.toString());
		user.setPassword(Constants.DEFAULT_ADMIN_PASS.toString());
		user.setType(UserType.INTERNAL);
		user.setEmail("defaultadministrator@example.com");
		user.setFullName("RP Admin");
		user.setDefaultProject(Constants.DEFAULT_ADMIN.toString() + PersonalProjectService.PERSONAL_PROJECT_POSTFIX);
		user.setIsExpired(false);
		user.getMetaInfo().setLastLogin(Calendar.getInstance().getTime());
		user.setRole(UserRole.ADMINISTRATOR);
		return user;
	});

	public static final Supplier<User> DEFAULT_USER = Suppliers.memoize(() -> {
		LOGGER.info("========== DEFAULT USER CREATION ==========");
		User user = new User();
		user.setLogin(Constants.DEFAULT_USER.toString());
		user.setPassword(Constants.DEFAULT_USER_PASS.toString());
		user.setType(UserType.INTERNAL);
		user.setEmail("defaulttester@example.com");
		user.setFullName("RP Tester");
		user.setDefaultProject(user.getLogin() + PersonalProjectService.PERSONAL_PROJECT_POSTFIX);
		user.setIsExpired(false);
		user.getMetaInfo().setLastLogin(Calendar.getInstance().getTime());
		user.setRole(UserRole.USER);
		return user;
	});

	public final Supplier<ServerSettings> DEFAULT_PROFILE = Suppliers.memoize(() -> {
		LOGGER.info("======= DEFAULT SERVER PROFILE CREATION =======");
		ServerSettings settings = new ServerSettings();
		settings.setId(DEFAULT_PROFILE_ID);
		settings.setActive(true);
		settings.setServerEmailDetails(
				new ServerEmailDetails(true, host, port, protocol, isEnable, false, false, username, password, "rp@epam.com"));
		settings.setAnalyticsDetails(ImmutableMap.<String, AnalyticsDetails>builder().put("all", new AnalyticsDetails(true)).build());
		settings.setInstanceId(UUID.randomUUID().toString());
		return settings;
	});

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ServerSettingsRepository serverSettingsRepository;

	@Autowired
	private PersonalProjectService personalProjectService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		/*
		 * if we didn't import default setting import them and set flag
		 */
		if (null == serverSettingsRepository.findOne(DEFAULT_PROFILE.get().getId())) {
			// Save non-existing user photo
			userRepository.uploadUserPhoto(Constants.NONAME_USER.toString(), NONAME_USER_PHOTO.get());

			// Super administrator
			if (null == userRepository.findOne(DEFAULT_ADMIN.get().getLogin())) {
				User user = DEFAULT_ADMIN.get();
				String photoId = userRepository.uploadUserPhoto(user.getLogin(), DEFAULT_ADMIN_PHOTO.get());
				user.setPhotoId(photoId);

				projectRepository.save(personalProjectService.generatePersonalProject(user));
				userRepository.save(user);
			}

			// Down-graded user (previous administrator)
			if (null == userRepository.findOne(DEFAULT_USER.get().getLogin())) {
				User user = DEFAULT_USER.get();
				String photoId = userRepository.uploadUserPhoto(user.getLogin(), DEMO_USER_PHOTO.get());
				user.setPhotoId(photoId);

				projectRepository.save(personalProjectService.generatePersonalProject(user));
				userRepository.save(user);
			}

			/* Create server settings repository with default profile */
			ServerSettings serverSettings = DEFAULT_PROFILE.get();
			serverSettingsRepository.save(serverSettings);

			if (null == serverSettings.getAnalyticsDetails()) {
				serverSettings.setAnalyticsDetails(
						ImmutableMap.<String, AnalyticsDetails>builder().put("all", new AnalyticsDetails(enableByDefault)).build());
				serverSettingsRepository.save(serverSettings);
			}
		}
	}
}
