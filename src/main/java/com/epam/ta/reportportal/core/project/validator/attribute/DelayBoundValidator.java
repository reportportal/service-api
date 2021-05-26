package com.epam.ta.reportportal.core.project.validator.attribute;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.exception.ReportPortalException;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;

public class DelayBoundValidator {

	private final List<DelayBoundLessRule> rules;

	public DelayBoundValidator(List<DelayBoundLessRule> rules) {
		this.rules = rules;
	}

	public void validate(Map<String, String> currentAttributes, Map<ProjectAttributeEnum, Long> newAttributes) {
		rules.forEach(rule -> {
			final Long lowerDelay = ofNullable(newAttributes.get(rule.getLower())).orElseGet(() -> getCurrentDelay(currentAttributes,
					rule.getLower()
			));
			final Long higherDelay = ofNullable(newAttributes.get(rule.getHigher())).orElseGet(() -> getCurrentDelay(currentAttributes,
					rule.getHigher()
			));
			BusinessRule.expect(lowerDelay <= higherDelay, equalTo(Boolean.TRUE))
					.verify(BAD_REQUEST_ERROR,
							Suppliers.formattedSupplier("Delay of '{}' should not be higher than '{}'",
									rule.getLower().getAttribute(),
									rule.getHigher().getAttribute()
							).toString()
					);
		});
	}

	private Long getCurrentDelay(Map<String, String> currentAttributes, ProjectAttributeEnum attribute) {
		return ofNullable(currentAttributes.get(attribute.getAttribute())).map(this::resolveDelay)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Attribute - {} was not found"),
						attribute.getAttribute()
				));
	}

	private Long resolveDelay(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException exc) {
			throw new ReportPortalException(BAD_REQUEST_ERROR, exc.getMessage());
		}
	}
}
