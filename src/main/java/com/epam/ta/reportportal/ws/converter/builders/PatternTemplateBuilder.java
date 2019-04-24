package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateBuilder implements Supplier<PatternTemplate> {

	private PatternTemplate patternTemplate;

	public PatternTemplateBuilder(CreatePatternTemplateRQ createPatternTemplateRQ) {
		patternTemplate = new PatternTemplate();
		patternTemplate.setTemplateType(PatternTemplateType.fromString(createPatternTemplateRQ.getType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Unknown pattern template type - '{}'", createPatternTemplateRQ.getType()).get()
				)));
		patternTemplate.setName(createPatternTemplateRQ.getName());
		patternTemplate.setValue(createPatternTemplateRQ.getValue());
		patternTemplate.setEnabled(createPatternTemplateRQ.getEnabled());
	}

	public PatternTemplateBuilder withName(String name) {
		patternTemplate.setName(name);
		return this;
	}

	public PatternTemplateBuilder withValue(String value) {
		patternTemplate.setValue(value);
		return this;
	}

	public PatternTemplateBuilder withType(PatternTemplateType type) {
		patternTemplate.setTemplateType(type);
		return this;
	}

	public PatternTemplateBuilder withEnabled(boolean isEnabled) {
		patternTemplate.setEnabled(isEnabled);
		return this;
	}

	public PatternTemplateBuilder withProjectId(Long projectId) {
		patternTemplate.setProjectId(projectId);
		return this;
	}

	@Override
	public PatternTemplate get() {
		return patternTemplate;
	}
}
