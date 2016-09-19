/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.epam.ta.reportportal.database.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.epam.ta.reportportal.commons.Constants;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.ProjectSettingsRepository;
import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.database.entity.project.InterruptionJobDelay;
import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import com.epam.ta.reportportal.database.entity.project.KeepScreenshotsDelay;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Initial deploy data
 * 
 * @author Andrei_Varabyeu
 * @author Andrei_Ramanchuk
 */
@Component
public class AddDemoProjectEventHandler implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddDemoProjectEventHandler.class);
	private static final AtomicBoolean IMPORTED_FLAG = new AtomicBoolean(false);
	private static final ProjectRole DEMO_ROLE = ProjectRole.MEMBER;
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

	/**
	 * Photo avatar for non-existing users
	 */
	public static final Supplier<BinaryData> NONAME_USER_PHOTO = Suppliers.memoize(() -> new BinaryData(MediaType.IMAGE_JPEG_VALUE, null,
			AddDemoProjectEventHandler.class.getClassLoader().getResourceAsStream("nonameUserPhoto.jpg")));

	/**
	 * Default user avatar
	 */
	public static final Supplier<BinaryData> DEMO_USER_PHOTO = Suppliers.memoize(() -> new BinaryData(MediaType.IMAGE_JPEG_VALUE, null,
			AddDemoProjectEventHandler.class.getClassLoader().getResourceAsStream("defaultUserPhoto.jpg")));

	/**
	 * Administrator avatar
	 */
	public static final Supplier<BinaryData> DEFAULT_ADMIN_PHOTO = Suppliers.memoize(() -> new BinaryData(MediaType.IMAGE_JPEG_VALUE, null,
			AddDemoProjectEventHandler.class.getClassLoader().getResourceAsStream("superAdminPhoto.jpg")));

	public static final Supplier<User> DEFAULT_ADMIN = Suppliers.memoize(() -> {
		LOGGER.info("========== DEFAULT ADMINISTRATOR CREATION ==========");
		User user = new User();
		user.setLogin(Constants.DEFAULT_ADMIN.toString());
		user.setPassword(Constants.DEFAULT_ADMIN_PASS.toString());
		user.setEntryType(EntryType.INTERNAL);
		user.setEmail("defaultadministrator@example.com");
		user.setFullName("RP Admin");
		user.setDefaultProject(Constants.DEFAULT_PROJECT.toString());
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
		user.setEntryType(EntryType.INTERNAL);
		user.setEmail("defaulttester@example.com");
		user.setFullName("RP Tester");
		user.setDefaultProject(Constants.DEFAULT_PROJECT.toString());
		user.setIsExpired(false);
		user.getMetaInfo().setLastLogin(Calendar.getInstance().getTime());
		user.setRole(UserRole.USER);
		return user;
	});

	public static final Supplier<Project> DEFAULT_PROJECT = Suppliers.memoize(() -> {
		LOGGER.info("========== DEFAULT PROJECT CREATION ==========");
		Project project = new Project();
		project.setName(Constants.DEFAULT_PROJECT.toString());
		project.setCustomer("some customer");
		project.setAddInfo("some additional info");
		project.setCreationDate(new Date());

		Map<String, UserConfig> users = new HashMap<>();
		users.put(DEFAULT_ADMIN.get().getId(), UserConfig.newOne().withProjectRole(DEMO_ROLE).withProposedRole(DEMO_ROLE));
		users.put(DEFAULT_USER.get().getId(), UserConfig.newOne().withProjectRole(DEMO_ROLE).withProposedRole(DEMO_ROLE));
		project.setUsers(users);
		project = setPredefinedConfiguration(project);
		project.getConfiguration().setExternalSystem(new ArrayList<>());
		return project;
	});

	public final Supplier<ServerSettings> DEFAULT_PROFILE = Suppliers.memoize(() -> {
		LOGGER.info("======= DEFAULT SERVER PROFILE CREATION =======");
		ServerSettings settings = new ServerSettings();
		settings.setId(DEFAULT_PROFILE_ID);
		settings.setActive(true);
		settings.setServerEmailConfig(new ServerEmailConfig(host, port, protocol, isEnable, username, password, isDebug));
		return settings;
	});

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ServerSettingsRepository serverSettingsRepository;

	@Autowired
	private ProjectSettingsRepository projectSettingsRepository;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		/*
		 * if we didn't import demo user and password, import them and set flag
		 * to TRUE
		 */
		if (IMPORTED_FLAG.compareAndSet(false, true)) {
			// Save non-existing user photo
			userRepository.saveUserPhoto(Constants.NONAME_USER.toString(), NONAME_USER_PHOTO.get());

			// Super administrator
			if (null == userRepository.findOne(DEFAULT_ADMIN.get().getLogin())) {
				User user = DEFAULT_ADMIN.get();
				String photoId = userRepository.saveUserPhoto(user.getLogin(), DEFAULT_ADMIN_PHOTO.get());
				user.setPhotoId(photoId);
				userRepository.save(user);
			}

			// Down-graded user (previous administrator)
			if (null == userRepository.findOne(DEFAULT_USER.get().getLogin())) {
				User user = DEFAULT_USER.get();
				String photoId = userRepository.saveUserPhoto(user.getLogin(), DEMO_USER_PHOTO.get());
				user.setPhotoId(photoId);
				userRepository.save(user);
			}

			if (null == projectRepository.findOne(DEFAULT_PROJECT.get().getId())) {
				projectRepository.save(DEFAULT_PROJECT.get());
			}

			/* Create server settings repository with default profile */
			if (null == serverSettingsRepository.findOne(DEFAULT_PROFILE.get().getId())) {
				serverSettingsRepository.save(DEFAULT_PROFILE.get());
			}

			if (null == projectSettingsRepository.findOne(DEFAULT_PROJECT.get().getId())) {
				projectSettingsRepository.save(new ProjectSettings(DEFAULT_PROJECT.get().getId()));
			}
		}
	}

	/**
	 * Added for unit testing.
	 * 
	 * @param flag
	 */
	public static void setImportedFlag(boolean flag) {
		synchronized (AddDemoProjectEventHandler.class) {
			IMPORTED_FLAG.set(flag);
		}
	}

	/**
	 * Set predefined project configuration
	 * 
	 * @param project
	 * @return Project
	 */
	/*
	 * TODO move default project configuration declaration in {@link
	 * ProjectUtils}
	 */
	private static Project setPredefinedConfiguration(Project project) {
		project.getConfiguration().setStatisticsCalculationStrategy(StatisticsCalculationStrategy.TEST_BASED);
		project.getConfiguration().setEntryType(EntryType.INTERNAL);
		project.getConfiguration().setInterruptJobTime(InterruptionJobDelay.ONE_DAY.getValue());
		project.getConfiguration().setKeepLogs(KeepLogsDelay.THREE_MONTHS.getValue());
		project.getConfiguration().setKeepScreenshots(KeepScreenshotsDelay.ONE_MONTH.getValue());
		project.getConfiguration().setProjectSpecific(ProjectSpecific.DEFAULT);
		project.getConfiguration().setIsAutoAnalyzerEnabled(false);

		/* Default email configuration */
		project = ProjectUtils.setDefaultEmailCofiguration(project);

		return project;
	}
}