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

package com.epam.ta.reportportal.core.integration.email;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */

import com.epam.ta.reportportal.core.integration.IntegrationService;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.project.email.LaunchStatsRule;
import com.epam.ta.reportportal.entity.project.email.SendCaseType;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Preconditions.NOT_EMPTY_COLLECTION;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.integration.email.EmailIntegrationUtil.*;
import static com.epam.ta.reportportal.dao.constant.WidgetRepositoryConstants.OWNER;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.getOwner;
import static com.epam.ta.reportportal.entity.project.email.SendCaseType.*;
import static com.epam.ta.reportportal.util.UserUtils.isEmailValid;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.USER_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Pavel Bortnik
 */
@Component
public class EmailIntegrationService implements IntegrationService {

	@Autowired
	private IntegrationTypeRepository integrationTypeRepository;

	@Override
	public boolean validateIntegrationParameters(Project project, Map<String, Object> integrationParameters) {
		expect(integrationParameters, notNull()).verify(BAD_REQUEST_ERROR, "Integration parameters should be provided.");
		List<Map<String, Object>> rules = getEmailRules(integrationParameters);
		expect(rules, NOT_EMPTY_COLLECTION).verify(BAD_REQUEST_ERROR, "At least one rule should be present.");
		rules.forEach(rule -> {
			String sendCase = getLaunchStatsValue(rule);
			expect(LaunchStatsRule.findByName(sendCase).isPresent(), equalTo(true)).verify(BAD_REQUEST_ERROR, sendCase);

			validateRecipients(project, getRuleValues(rule, RECIPIENTS));
			validateLaunchName(getRuleValues(rule, LAUNCH_NAME_RULE));
			rule.put(LAUNCH_TAG_RULE.getCaseTypeString(), validateTags(getRuleValues(rule, LAUNCH_TAG_RULE)));
		});

		return true;
	}

	/**
	 * Setup default project email configuration
	 *
	 * @param project Project
	 * @return project object with default email config
	 */
	public Project setDefaultEmailConfiguration(Project project) {
		Optional<IntegrationType> email = integrationTypeRepository.findByNameAndIntegrationGroup(EMAIL, IntegrationGroupEnum.NOTIFICATION);
		expect(email, Optional::isPresent).verify(ErrorType.INTEGRATION_NOT_FOUND, EMAIL);

		Map<String, Object> defaultCases = new HashMap<>();
		defaultCases.put(RECIPIENTS.getCaseTypeString(), Lists.newArrayList(OWNER));
		defaultCases.put(SendCaseType.LAUNCH_STATS_RULE.getCaseTypeString(), Lists.newArrayList(LaunchStatsRule.ALWAYS.getRuleString()));
		IntegrationParams integrationParams = new IntegrationParams(defaultCases);

		Integration integration = new Integration();
		integration.setParams(integrationParams);
		integration.setProject(project);
		integration.setType(email.get());

		project.getIntegrations().add(integration);
		return project;
	}

	/**
	 * Exclude specified project users
	 *
	 * @param projectUsers Users
	 * @param project      Project
	 */
	public void excludeProjectRecipients(Iterable<ProjectUser> projectUsers, Project project) {
		if (projectUsers != null) {
			List<String> toExclude = stream(projectUsers.spliterator(), false).map(projectUser -> asList(projectUser.getUser()
					.getEmail()
					.toLowerCase(), projectUser.getUser().getLogin().toLowerCase())).flatMap(List::stream).collect(toList());
			Integration integration = getEmailIntegration(project).orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND,
					EMAIL
			));
			List<Map<String, Object>> rules = getEmailRules(integration.getParams().getParams());
			rules.forEach(rule -> getRuleValues(rule, RECIPIENTS).removeAll(toExclude));
		}
	}

	/**
	 * Update specified project recipient
	 *
	 * @param oldEmail Old email
	 * @param newEmail New email
	 * @param project  Project to update
	 * @return Updated project
	 */
	public Project updateProjectRecipients(String oldEmail, String newEmail, Project project) {
		if ((null != oldEmail) && (null != newEmail)) {
			Integration integration = getEmailIntegration(project).orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND,
					EMAIL
			));
			List<Map<String, Object>> rules = getEmailRules(integration.getParams().getParams());
			rules.forEach(rule -> getRuleValues(rule, RECIPIENTS).replaceAll(it -> {
				if (it.equalsIgnoreCase(oldEmail)) {
					it = it.replaceAll(oldEmail, newEmail);
				}
				return it;
			}));
		}
		return project;
	}

	private List<String> validateTags(List<String> strings) {
		return strings.stream().map(tag -> {
			expect(isNullOrEmpty(tag), equalTo(false)).verify(BAD_REQUEST_ERROR,
					"Tags values cannot be empty. Please specify it or not include in request."
			);
			return tag.trim();
		}).distinct().collect(toList());
	}

	private void validateRecipients(Project project, List<String> recipients) {
		expect(recipients, NOT_EMPTY_COLLECTION).verify(BAD_REQUEST_ERROR, "Recipients list should not be null or empty");
		recipients.stream().distinct().forEach(recipient -> {
			if (recipient.contains("@")) {
				expect(isEmailValid(recipient), equalTo(true)).verify(BAD_REQUEST_ERROR,
						formattedSupplier("Provided recipient email '{}' is invalid", recipient)
				);
			} else {
				final String login = recipient.trim();
				expect(MIN_LOGIN_LENGTH <= login.length() && login.length() <= MAX_LOGIN_LENGTH, equalTo(true)).verify(BAD_REQUEST_ERROR,
						"Acceptable login length  [" + MIN_LOGIN_LENGTH + ".." + MAX_LOGIN_LENGTH + "]"
				);
				if (!getOwner().equals(login)) {
					expect(ProjectUtils.doesHaveUser(project, login.toLowerCase()), equalTo(true)).verify(USER_NOT_FOUND,
							login,
							String.format("User not found in project %s", project.getId())
					);
				}
			}
		});
	}

	private void validateLaunchName(List<String> names) {
		if (CollectionUtils.isNotEmpty(names)) {
			names.forEach(name -> {
				expect(isNullOrEmpty(name), equalTo(false)).verify(BAD_REQUEST_ERROR,
						"Launch name values cannot be empty. Please specify it or not include in request."
				);
				expect(name.length() <= MAX_NAME_LENGTH, equalTo(true)).verify(BAD_REQUEST_ERROR,
						formattedSupplier("One of provided launch names '{}' is too long. Acceptable name length is [1..256]", name)
				);
			});
		}
	}
}
