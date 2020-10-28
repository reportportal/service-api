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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.attachment.DeleteProjectAttachmentsEvent;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.PersonalProjectService;
import com.epam.ta.reportportal.ws.converter.builders.UserBuilder;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import io.minio.MinioClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class FlushingDataJob implements Job {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
	private static final String SUPERADMIN_PERSONAL = "superadmin_personal";
	private static final String SUPERADMIN = "superadmin";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PersonalProjectService personalProjectService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LogIndexer logIndexer;

	@Autowired
	private IssueTypeRepository issueTypeRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private UserBinaryDataService dataStore;

	@Autowired
	private MinioClient minioClient;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${datastore.minio.bucketPrefix}")
	private String bucketPrefix;

	@Override
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public void execute(JobExecutionContext context) {
		LOGGER.info("Flushing demo instance data is starting...");
		truncateTables();
		projectRepository.findAllProjectNames()
				.stream()
				.filter(it -> !it.equalsIgnoreCase(SUPERADMIN_PERSONAL))
				.collect(Collectors.toList())
				.forEach(name -> projectRepository.findByName(name).ifPresent(this::deleteProject));
		userRepository.findAll().stream().filter(it -> !it.getLogin().equalsIgnoreCase(SUPERADMIN)).forEach(this::deleteUser);
		restartSequences();
		createDefaultUser();
		LOGGER.info("Flushing demo instance data finished");
	}

	/**
	 * Get exclusive lock. Kill all running transactions. Truncate tables
	 */
	private void truncateTables() {
		jdbcTemplate.execute("BEGIN; " + "SELECT pg_advisory_xact_lock(1);"
				+ "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'reportportal'\n"
				+ "AND pid <> pg_backend_pid()\n"
				+ "AND state IN ('idle', 'idle in transaction', 'idle in transaction (aborted)', 'disabled'); "
				+ "TRUNCATE TABLE launch RESTART IDENTITY CASCADE;" + "TRUNCATE TABLE activity RESTART IDENTITY CASCADE;"
				+ "TRUNCATE TABLE shareable_entity RESTART IDENTITY CASCADE;" + "TRUNCATE TABLE ticket RESTART IDENTITY CASCADE;"
				+ "TRUNCATE TABLE issue_ticket RESTART IDENTITY CASCADE;" + "COMMIT;");
	}

	private void restartSequences() {
		jdbcTemplate.execute("ALTER SEQUENCE project_id_seq RESTART WITH 2");
		jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART WITH 2");
		jdbcTemplate.execute("ALTER SEQUENCE oauth_access_token_id_seq RESTART WITH 2");
		jdbcTemplate.execute("ALTER SEQUENCE project_attribute_attribute_id_seq RESTART WITH 15");
		jdbcTemplate.execute("ALTER SEQUENCE statistics_field_sf_id_seq RESTART WITH 15");
	}

	private void createDefaultUser() {
		final CreateUserRQFull request = new CreateUserRQFull();
		request.setLogin("default");
		request.setPassword(passwordEncoder.encode("1q2w3e"));
		request.setEmail("defaultemail@domain.com");
		User user = new UserBuilder().addCreateUserFullRQ(request)
				.addUserRole(UserRole.USER)
				.addPassword(passwordEncoder.encode(request.getPassword()))
				.get();
		projectRepository.save(personalProjectService.generatePersonalProject(user));
		userRepository.save(user);
		LOGGER.info("Default user has been successfully created.");
	}

	private void deleteUser(User user) {
		dataStore.deleteUserPhoto(user);
		userRepository.delete(user);
		userRepository.flush();
		LOGGER.info("User with id = '" + user.getId() + "' has been successfully deleted.");
	}

	private void deleteProject(Project project) {
		Set<Long> defaultIssueTypeIds = issueTypeRepository.getDefaultIssueTypes()
				.stream()
				.map(IssueType::getId)
				.collect(Collectors.toSet());
		Set<IssueType> issueTypesToRemove = project.getProjectIssueTypes()
				.stream()
				.map(ProjectIssueType::getIssueType)
				.filter(issueType -> !defaultIssueTypeIds.contains(issueType.getId()))
				.collect(Collectors.toSet());
		projectRepository.delete(project);
		issueTypeRepository.deleteAll(issueTypesToRemove);
		try {
			minioClient.deleteBucketLifeCycle(bucketPrefix + project.getId());
		} catch (Exception e) {
			LOGGER.warn("Cannot delete attachments bucket " + bucketPrefix + project.getId());
		}
		logIndexer.deleteIndex(project.getId());
		projectRepository.flush();
		eventPublisher.publishEvent(new DeleteProjectAttachmentsEvent(project.getId()));
		LOGGER.info("Project with id = '" + project.getId() + "' has been successfully deleted.");
	}
}
