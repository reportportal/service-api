package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class EmailConfigConverter {

	private EmailConfigConverter() {
		//static only
	}

	public final static Function<Set<SenderCase>, ProjectNotificationConfigDTO> TO_RESOURCE = senderCaseSet -> {
		ProjectNotificationConfigDTO dto = new ProjectNotificationConfigDTO();

		ofNullable(senderCaseSet).ifPresent(senderCases -> dto.setSenderCases(senderCases.stream()
				.map(EmailConfigConverter.TO_CASE_RESOURCE)
				.collect(Collectors.toList())));

		return dto;
	};

	public final static Function<SenderCase, SenderCaseDTO> TO_CASE_RESOURCE = model -> {
		Preconditions.checkNotNull(model);
		SenderCaseDTO resource = new SenderCaseDTO();
		resource.setLaunchNames(Lists.newArrayList(model.getLaunchNames()));
		resource.setAttributes(Lists.newArrayList(model.getLaunchAttributes()));
		resource.setSendCase(model.getSendCase().getCaseString());
		resource.setRecipients(Lists.newArrayList(model.getRecipients()));
		return resource;
	};

	public final static Function<SenderCaseDTO, SenderCase> TO_CASE_MODEL = resource -> {
		SenderCase senderCase = new SenderCase();
		senderCase.setLaunchAttributes(Sets.newHashSet(resource.getAttributes()));
		senderCase.setLaunchNames(Sets.newHashSet(resource.getLaunchNames()));
		senderCase.setRecipients(Sets.newHashSet(resource.getRecipients()));
		senderCase.setSendCase(SendCase.findByName(resource.getSendCase())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Incorrect send case type " + resource.getSendCase()
				)));
		return senderCase;
	};
}
