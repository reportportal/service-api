package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.model.project.config.pattern.PatternTemplateResource;

import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateConverter {

	private PatternTemplateConverter() {
		//static only
	}

	public static final Function<PatternTemplate, PatternTemplateResource> TO_RESOURCE = patternTemplate -> {
		PatternTemplateResource resource = new PatternTemplateResource();
		resource.setId(patternTemplate.getId());
		resource.setType(patternTemplate.getTemplateType().name());
		resource.setName(patternTemplate.getName());
		resource.setValue(patternTemplate.getValue());
		resource.setEnabled(patternTemplate.isEnabled());

		return resource;
	};
}
