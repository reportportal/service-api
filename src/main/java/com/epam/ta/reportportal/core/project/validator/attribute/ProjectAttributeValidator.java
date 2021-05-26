package com.epam.ta.reportportal.core.project.validator.attribute;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

public class ProjectAttributeValidator {

	private final DelayBoundValidator delayBoundValidator;

	public ProjectAttributeValidator(DelayBoundValidator delayBoundValidator) {
		this.delayBoundValidator = delayBoundValidator;
	}

	public void verifyProjectAttributes(Map<String, String> currentAttributes, Map<String, String> newAttributes) {
		Set<String> incompatibleAttributes = newAttributes.keySet()
				.stream()
				.filter(it -> !ProjectAttributeEnum.isPresent(it))
				.collect(toSet());
		expect(incompatibleAttributes, Set::isEmpty).verify(BAD_REQUEST_ERROR, incompatibleAttributes);

		ofNullable(newAttributes.get(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute())).ifPresent(analyzerMode -> expect(AnalyzeMode.fromString(
				analyzerMode), isPresent()).verify(ErrorType.BAD_REQUEST_ERROR, analyzerMode));
		final Map<ProjectAttributeEnum, Long> delays = validateDelays(newAttributes,
				List.of(ProjectAttributeEnum.KEEP_SCREENSHOTS, ProjectAttributeEnum.KEEP_LOGS, ProjectAttributeEnum.KEEP_LAUNCHES)
		);

		delayBoundValidator.validate(currentAttributes, delays);
	}

	private Map<ProjectAttributeEnum, Long> validateDelays(Map<String, String> attributes, List<ProjectAttributeEnum> projectAttributes) {
		return projectAttributes.stream()
				.filter(it -> attributes.containsKey(it.getAttribute()))
				.collect(Collectors.toMap(a -> a, a -> getDelay(attributes.get(a.getAttribute()))));
	}

	private Long getDelay(String value) {
		try {
			final long delay = Long.parseLong(value);
			BusinessRule.expect(delay, d -> d >= 0).verify(BAD_REQUEST_ERROR, "Delay attribute value should be greater than 0");
			return delay;
		} catch (NumberFormatException exc) {
			throw new ReportPortalException(BAD_REQUEST_ERROR, exc.getMessage());
		}
	}
}
