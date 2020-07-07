package com.epam.ta.reportportal.core.project.content.remover.impl;

import com.epam.ta.reportportal.core.project.content.remover.ProjectContentRemover;
import com.epam.ta.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ProjectContentRemoverImpl implements ProjectContentRemover {

	private final WidgetRepository widgetRepository;
	private final List<WidgetContentRemover> widgetContentRemovers;

	@Autowired
	public ProjectContentRemoverImpl(WidgetRepository widgetRepository, List<WidgetContentRemover> widgetContentRemovers) {
		this.widgetRepository = widgetRepository;
		this.widgetContentRemovers = widgetContentRemovers;
	}

	@Override
	public void removeContent(Project project) {
		List<Widget> widgets = widgetRepository.findAllByProjectIdAndWidgetTypeIn(project.getId(),
				Collections.singletonList(WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType())
		);
		widgets.forEach(w -> widgetContentRemovers.forEach(remover -> remover.removeContent(w)));
	}
}
