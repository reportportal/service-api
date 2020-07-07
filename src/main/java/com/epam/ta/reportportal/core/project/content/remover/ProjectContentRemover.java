package com.epam.ta.reportportal.core.project.content.remover;

import com.epam.ta.reportportal.entity.project.Project;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ProjectContentRemover {

	void removeContent(Project project);
}
