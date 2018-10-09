package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

import java.util.Set;
import java.util.function.BiFunction;

public class EmailConfigConverter {

	private EmailConfigConverter() {
		//static only
	}

	public static BiFunction<Set<ProjectAttribute>, Set<EmailSenderCase>, ProjectEmailConfigDTO> TO_RESOURCE = (pa, es) -> {
		ProjectEmailConfigDTO dto = new ProjectEmailConfigDTO();

		/*pa.stream()
				.filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.EMAIL_FROM.getValue()))
				.findFirst().ifPresent(it -> dto.setFrom(it.getValue()));


		pa.stream()
				.filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.EMAIL_ENABLED.getValue()))
				.findFirst()
				.ifPresent(it -> dto.setEmailEnabled(BooleanUtils.toBoolean(it.getValue())));


		dto.setEmailCases(es.stream().map(item -> new EmailSenderCaseDTO(
				item.getRecipients(),
				item.getSendCase().toString(),
				item.getLaunches().stream().map(Launch::getName).collect(Collectors.toList()),
				item.getTags().stream().map(LaunchTag::getValue).collect(Collectors.toList())
		)).collect(Collectors.toList()));*/

		return dto;
	};

/*	public static Function<Activity, ActivityResource> c = t -> {
		ActivityResource activityResource = new ActivityResource();
		activityResource.setDetails(t.getDetails());
	};*/
}
