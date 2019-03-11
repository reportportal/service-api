package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.email.LaunchAttribute;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class NotificationConfigConverter {

	private NotificationConfigConverter() {
		//static only
	}

	public final static Function<Set<SenderCase>, List<SenderCaseDTO>> TO_RESOURCE = senderCaseSet -> senderCaseSet.stream()
			.map(NotificationConfigConverter.TO_CASE_RESOURCE)
			.collect(Collectors.toList());

	public static final Function<LaunchAttributeRule, LaunchAttribute> TO_ATTRIBUTE_RULE_RESOURCE = model -> {
		LaunchAttribute launchAttribute = new LaunchAttribute();
		launchAttribute.setKey(model.getKey());
		launchAttribute.setValue(model.getValue());

		return launchAttribute;
	};

	public final static Function<SenderCase, SenderCaseDTO> TO_CASE_RESOURCE = model -> {
		Preconditions.checkNotNull(model);
		SenderCaseDTO resource = new SenderCaseDTO();
		resource.setLaunchNames(Lists.newArrayList(model.getLaunchNames()));
		ofNullable(model.getLaunchAttributeRules()).ifPresent(launchAttributeRules -> resource.setAttributes(launchAttributeRules.stream()
				.map(TO_ATTRIBUTE_RULE_RESOURCE)
				.collect(Collectors.toSet())));
		resource.setSendCase(model.getSendCase().getCaseString());
		resource.setRecipients(Lists.newArrayList(model.getRecipients()));
		return resource;
	};

	public static final Function<LaunchAttribute, LaunchAttributeRule> TO_ATTRIBUTE_RULE_MODEL = resource -> {

		LaunchAttributeRule launchAttributeRule = new LaunchAttributeRule();
		launchAttributeRule.setKey(resource.getKey());
		launchAttributeRule.setValue(resource.getValue());

		return launchAttributeRule;
	};

	public final static Function<SenderCaseDTO, SenderCase> TO_CASE_MODEL = resource -> {
		SenderCase senderCase = new SenderCase();
		ofNullable(resource.getAttributes()).ifPresent(attributes -> senderCase.setLaunchAttributeRules(attributes.stream()
				.map(attribute -> {
					LaunchAttributeRule launchAttributeRule = TO_ATTRIBUTE_RULE_MODEL.apply(attribute);
					launchAttributeRule.setSenderCase(senderCase);
					return launchAttributeRule;
				})
				.collect(Collectors.toSet())));
		senderCase.setLaunchNames(Sets.newHashSet(resource.getLaunchNames()));
		senderCase.setRecipients(Sets.newHashSet(resource.getRecipients()));
		senderCase.setSendCase(SendCase.findByName(resource.getSendCase())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Incorrect send case type " + resource.getSendCase()
				)));
		return senderCase;
	};
}
