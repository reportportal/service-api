package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreatePatternTemplateHandler {

	/**
	 * Create {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate} entity for specified {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param projectId               {@link com.epam.ta.reportportal.entity.pattern.PatternTemplate#projectId}
	 * @param createPatternTemplateRQ {@link CreatePatternTemplateRQ}
	 * @return {@link EntryCreatedRS}
	 */
	EntryCreatedRS createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ);
}
