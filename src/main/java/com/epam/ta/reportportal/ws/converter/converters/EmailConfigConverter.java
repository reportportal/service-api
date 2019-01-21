package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.SendCase;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class EmailConfigConverter {

	private EmailConfigConverter() {
		//static only
	}

	public final static BiFunction<Set<ProjectAttribute>, Set<EmailSenderCase>, ProjectEmailConfigDTO> TO_RESOURCE = (pa, es) -> {
		ProjectEmailConfigDTO dto = new ProjectEmailConfigDTO();

		dto.setEmailCases(es.stream().map(EmailConfigConverter.TO_CASE_RESOURCE).collect(Collectors.toList()));

		return dto;
	};

	public final static Function<EmailSenderCase, EmailSenderCaseDTO> TO_CASE_RESOURCE = model -> {
		Preconditions.checkNotNull(model);
		EmailSenderCaseDTO resource = new EmailSenderCaseDTO();
		resource.setLaunchNames(Lists.newArrayList(model.getLaunchNames()));
		resource.setAttributes(Lists.newArrayList(model.getLaunchAttributes()));
		resource.setSendCase(model.getSendCase().getCaseString());
		resource.setRecipients(Lists.newArrayList(model.getRecipients()));
		return resource;
	};

	public final static Function<EmailSenderCaseDTO, EmailSenderCase> TO_CASE_MODEL = resource -> {
		EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setLaunchAttributes(Sets.newHashSet(resource.getAttributes()));
		emailSenderCase.setLaunchNames(Sets.newHashSet(resource.getLaunchNames()));
		emailSenderCase.setRecipients(Sets.newHashSet(resource.getRecipients()));
		emailSenderCase.setSendCase(SendCase.findByName(resource.getSendCase())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Incorrect send case type " + resource.getSendCase()
				)));
		return emailSenderCase;
	};
}
