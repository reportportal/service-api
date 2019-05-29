package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PatternTemplateBuilderTest {


	@Test
	void patternTemplateBuilder() {
		CreatePatternTemplateRQ createPatternTemplateRQ = new CreatePatternTemplateRQ();
		String name = "name";
		String type = "STRING";
		boolean enabled = true;
		String value = "qwe";
		createPatternTemplateRQ.setName(name);
		createPatternTemplateRQ.setType(type);
		createPatternTemplateRQ.setEnabled(enabled);
		createPatternTemplateRQ.setValue(value);

		PatternTemplate patternTemplate = new PatternTemplateBuilder().withCreateRequest(createPatternTemplateRQ).get();

		assertEquals(name, patternTemplate.getName());
		assertEquals(type, patternTemplate.getTemplateType().name());
		assertEquals(enabled, patternTemplate.isEnabled());
		assertEquals(value, patternTemplate.getValue());
	}
}