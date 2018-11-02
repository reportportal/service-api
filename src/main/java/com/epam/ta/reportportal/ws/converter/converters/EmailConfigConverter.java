package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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

		dto.setEmailCases(es.stream().map(item -> {
					EmailSenderCaseDTO emailSenderCaseDTO = new EmailSenderCaseDTO();
					emailSenderCaseDTO.setRecipients(item.getRecipients());
					emailSenderCaseDTO.setSendCase(item.getSendCase().getCaseString());

					return emailSenderCaseDTO;
				}

				//				item.getLaunches().stream().map(Launch::getName).collect(Collectors.toList()),
				//				item.getTags().stream().map(LaunchTag::getValue).collect(Collectors.toList())
		).collect(Collectors.toList()));

		return dto;
	};
}
