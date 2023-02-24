/*
 *
 * Copyright 2022 EPAM Systems
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
 *
 */

package com.epam.ta.reportportal.core.project.validator.notification;

import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.util.email.EmailRulesValidator;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.SendCase.findByName;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class ProjectNotificationValidator {

	private final SenderCaseRepository senderCaseRepository;

	@Autowired
	public ProjectNotificationValidator(SenderCaseRepository senderCaseRepository) {
		this.senderCaseRepository = senderCaseRepository;
	}

	public void validateCreateRQ(Project project, SenderCaseDTO senderCaseDTO) {
		validateRecipients(senderCaseDTO);

		normalizeCreateNotificationRQ(project, senderCaseDTO);

		Optional<SenderCaseDTO> duplicate = senderCaseRepository.findAllByProjectId(project.getId())
				.stream()
				.map(NotificationConfigConverter.TO_CASE_RESOURCE)
				.filter(existing -> equalsWithoutRuleName(existing, senderCaseDTO))
				.findFirst();
		expect(duplicate, Optional::isEmpty).verify(BAD_REQUEST_ERROR, "Project email settings contain duplicate cases");
	}

	public void validateUpdateRQ(Project project, SenderCaseDTO senderCaseDTO) {
		validateRecipients(senderCaseDTO);

		normalizeCreateNotificationRQ(project, senderCaseDTO);

		Optional<SenderCaseDTO> duplicate = senderCaseRepository.findAllByProjectId(project.getId())
				.stream()
				.filter(senderCase -> !Objects.equals(senderCase.getId(), senderCaseDTO.getId()))
				.map(NotificationConfigConverter.TO_CASE_RESOURCE)
				.filter(o1 -> equalsWithoutRuleName(o1, senderCaseDTO))
				.findFirst();
		expect(duplicate, Optional::isEmpty).verify(BAD_REQUEST_ERROR, "Project email settings contain duplicate cases");
	}

	private void validateRecipients(SenderCaseDTO senderCaseDTO) {
		List<String> recipients = senderCaseDTO.getRecipients();
		expect(findByName(senderCaseDTO.getSendCase()), Optional::isPresent)
				.verify(BAD_REQUEST_ERROR, senderCaseDTO.getSendCase());
		expect(recipients, notNull()).verify(BAD_REQUEST_ERROR, "Recipients list should not be null");
		expect(recipients.isEmpty(), equalTo(false))
				.verify(BAD_REQUEST_ERROR, formattedSupplier("Empty recipients list for email case '{}' ", senderCaseDTO));
	}

	private void normalizeCreateNotificationRQ(Project project, SenderCaseDTO createNotificationRQ) {
		createNotificationRQ.setRecipients(
				createNotificationRQ.getRecipients().stream().map(recipient -> {
					EmailRulesValidator.validateRecipient(project, recipient);
					return recipient.trim();
				}).distinct().collect(toList())
		);
		ofNullable(createNotificationRQ.getLaunchNames()).ifPresent(launchNames -> createNotificationRQ.setLaunchNames(
				launchNames.stream().map(name -> {
					EmailRulesValidator.validateLaunchName(name);
					return name.trim();
				}).distinct().collect(toList()))
		);
		ofNullable(createNotificationRQ.getAttributes()).ifPresent(attributes -> createNotificationRQ.setAttributes(
				attributes.stream().peek(attribute -> {
					EmailRulesValidator.validateLaunchAttribute(attribute);
					attribute.setValue(attribute.getValue().trim());
				}).collect(Collectors.toSet()))
		);
	}

	private boolean equalsWithoutRuleName(SenderCaseDTO senderCase, SenderCaseDTO toCompare) {
		return CollectionUtils.isEqualCollection(senderCase.getRecipients(), toCompare.getRecipients())
				&& Objects.equals(senderCase.getSendCase(), toCompare.getSendCase())
				&& CollectionUtils.isEqualCollection(senderCase.getLaunchNames(), toCompare.getLaunchNames())
				&& CollectionUtils.isEqualCollection(senderCase.getAttributes(), toCompare.getAttributes());
	}
}
