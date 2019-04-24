package com.epam.ta.reportportal.core.pattern.impl;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.pattern.CreatePatternTemplateHandler;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.PatternTemplateBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateRegexPatternTemplateHandler implements CreatePatternTemplateHandler {

	private final PatternTemplateRepository patternTemplateRepository;

	@Autowired
	public CreateRegexPatternTemplateHandler(PatternTemplateRepository patternTemplateRepository) {
		this.patternTemplateRepository = patternTemplateRepository;
	}

	@Override
	public EntryCreatedRS createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ) {

		try {
			Pattern.compile(createPatternTemplateRQ.getValue());
		} catch (PatternSyntaxException ex) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
					Suppliers.formattedSupplier("Provided regex pattern - '{}' is invalid", createPatternTemplateRQ.getValue()).get()
			);
		}

		BusinessRule.expect(patternTemplateRepository.existsByProjectIdAndNameIgnoreCase(projectId, createPatternTemplateRQ.getName()),
				equalTo(false)
		).verify(ErrorType.RESOURCE_ALREADY_EXISTS, createPatternTemplateRQ.getName());

		PatternTemplate patternTemplate = new PatternTemplateBuilder(createPatternTemplateRQ).withProjectId(projectId).get();
		patternTemplateRepository.save(patternTemplate);

		return new EntryCreatedRS(patternTemplate.getId());
	}
}
