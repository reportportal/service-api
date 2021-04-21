/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.analyzer.pattern.impl;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateRegexPatternTemplateHandler extends CreatePatternTemplateHandlerImpl {

	@Autowired
	public CreateRegexPatternTemplateHandler(PatternTemplateRepository patternTemplateRepository) {
		super(patternTemplateRepository);
	}

	@Override
	public PatternTemplate createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ) {
		try {
			patternTemplateRepository.validateRegex(createPatternTemplateRQ.getValue());
		} catch (PersistenceException ex) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
					Suppliers.formattedSupplier("Provided regex pattern - '{}' is invalid", createPatternTemplateRQ.getValue()).get()
			);
		}
		return super.createPatternTemplate(projectId, createPatternTemplateRQ);
	}
}
