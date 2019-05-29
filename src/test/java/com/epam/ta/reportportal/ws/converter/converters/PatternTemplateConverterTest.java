package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import com.epam.ta.reportportal.ws.model.project.config.pattern.PatternTemplateResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PatternTemplateConverterTest {

	@Test
	public void toResourceTest() {

		PatternTemplate patternTemplate = get();

		PatternTemplateResource resource = PatternTemplateConverter.TO_RESOURCE.apply(patternTemplate);

		assertEquals(patternTemplate.getId(), resource.getId());
		assertEquals(patternTemplate.getTemplateType().name(), resource.getType());
		assertEquals(patternTemplate.getName(), resource.getName());
		assertEquals(patternTemplate.getValue(), resource.getValue());
		assertEquals(patternTemplate.isEnabled(), resource.getEnabled());
	}

	@Test
	public void toActivityResourceTest() {

		PatternTemplate patternTemplate = get();

		PatternTemplateActivityResource resource = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(patternTemplate);

		assertEquals(patternTemplate.getId(), resource.getId());
		assertEquals(patternTemplate.getProjectId(), resource.getProjectId());
		assertEquals(patternTemplate.getName(), resource.getName());
	}

	private PatternTemplate get() {
		PatternTemplate patternTemplate = new PatternTemplate();
		patternTemplate.setId(1L);
		patternTemplate.setProjectId(1L);
		patternTemplate.setTemplateType(PatternTemplateType.STRING);
		patternTemplate.setEnabled(true);
		patternTemplate.setValue("qwe");
		patternTemplate.setName("name");
		return patternTemplate;
	}

}