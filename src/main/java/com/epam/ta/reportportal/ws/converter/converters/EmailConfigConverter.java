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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.entity.project.email.SendCaseType;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public class EmailConfigConverter {

	private EmailConfigConverter() {
		//static only
	}

	public final static BiFunction<Set<ProjectAttribute>, Set<EmailSenderCase>, ProjectEmailConfigDTO> TO_RESOURCE = (pa, es) -> {
		ProjectEmailConfigDTO dto = new ProjectEmailConfigDTO();
		pa.stream()
				.filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.EMAIL_FROM.getAttribute()))
				.findFirst()
				.ifPresent(it -> dto.setFrom(it.getValue()));

		pa.stream()
				.filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.EMAIL_ENABLED.getAttribute()))
				.findFirst()
				.ifPresent(it -> dto.setEmailEnabled(BooleanUtils.toBoolean(it.getValue())));

		dto.setEmailCases(es.stream().map(EmailConfigConverter.TO_CASE_RESOURCE).collect(Collectors.toList()));

		return dto;
	};

	public final static Function<EmailSenderCase, EmailSenderCaseDTO> TO_CASE_RESOURCE = model -> {
		Preconditions.checkNotNull(model);
		EmailSenderCaseDTO resource = new EmailSenderCaseDTO();
		resource.setCases(model.getSenderCaseList()
				.stream()
				.collect(Collectors.toMap(it -> it.getKey().getCaseTypeString(), it -> Lists.newArrayList(it.getValues()))));
		return resource;
	};

	public final static Function<EmailSenderCaseDTO, EmailSenderCase> TO_CASE_MODEL = resource -> {
		EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSenderCaseList(resource.getCases()
				.entrySet()
				.stream()
				.map(e -> new SenderCase.SenderCaseBuilder().withKey(SendCaseType.findByName(e.getKey())
						.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
								"Incorrect send case type " + e.getKey()
						)))
						.withValues(Sets.newHashSet(e.getValue()))
						.get())
				.collect(Collectors.toSet()));
		return emailSenderCase;
	};
}
