package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreatePatternTemplateHandler {

	/**
	 * @param projectId
	 * @param createPatternTemplateRQ
	 * @return
	 */
	EntryCreatedRS createPatternTemplate(Long projectId, CreatePatternTemplateRQ createPatternTemplateRQ);
}
