package com.epam.ta.reportportal.core.analyzer.pattern.impl;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.analyzer.pattern.CreatePatternTemplateHandler;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.converter.builders.PatternTemplateBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.springframework.beans.factory.annotation.Autowired;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CreatePatternTemplateHandlerImpl implements CreatePatternTemplateHandler {

	protected final PatternTemplateRepository patternTemplateRepository;

	@Autowired
	public CreatePatternTemplateHandlerImpl(PatternTemplateRepository patternTemplateRepository) {
		this.patternTemplateRepository = patternTemplateRepository;
	}

	@Override
	public PatternTemplate createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ) {
		final String name = createPatternTemplateRQ.getName().trim();
		BusinessRule.expect(patternTemplateRepository.existsByProjectIdAndNameIgnoreCase(projectId, name), equalTo(false))
				.verify(ErrorType.RESOURCE_ALREADY_EXISTS, name);
		PatternTemplate patternTemplate = new PatternTemplateBuilder().withCreateRequest(createPatternTemplateRQ)
				.withName(name)
				.withProjectId(projectId)
				.get();
		return patternTemplateRepository.save(patternTemplate);
	}
}
