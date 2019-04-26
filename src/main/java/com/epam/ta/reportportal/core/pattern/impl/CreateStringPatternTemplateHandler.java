package com.epam.ta.reportportal.core.pattern.impl;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.pattern.CreatePatternTemplateHandler;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.converter.builders.PatternTemplateBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateStringPatternTemplateHandler implements CreatePatternTemplateHandler {

	private final PatternTemplateRepository patternTemplateRepository;

	@Autowired
	public CreateStringPatternTemplateHandler(PatternTemplateRepository patternTemplateRepository) {
		this.patternTemplateRepository = patternTemplateRepository;
	}

	@Override
	public EntryCreatedRS createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ) {

		BusinessRule.expect(patternTemplateRepository.existsByProjectIdAndNameIgnoreCase(projectId, createPatternTemplateRQ.getName()),
				equalTo(false)
		).verify(ErrorType.RESOURCE_ALREADY_EXISTS, createPatternTemplateRQ.getName());

		PatternTemplate patternTemplate = new PatternTemplateBuilder().withCreateRequest(createPatternTemplateRQ)
				.withProjectId(projectId)
				.get();
		patternTemplateRepository.save(patternTemplate);

		return new EntryCreatedRS(patternTemplate.getId());

	}
}
