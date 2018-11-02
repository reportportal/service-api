package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
		resource.setLaunchNames(Lists.newArrayList(model.getLaunchNames()));
		resource.setTags(Lists.newArrayList(model.getLaunchTags()));
		resource.setSendCase(model.getSendCase().getCaseString());
		resource.setRecipients(Lists.newArrayList(model.getRecipients()));
		return resource;
	};
}
