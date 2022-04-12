package com.epam.ta.reportportal.core.remover.project;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ProjectWidgetRemover implements ContentRemover<Project> {

	private final WidgetRepository widgetRepository;
	private final WidgetContentRemover widgetContentRemover;

	@Autowired
	public ProjectWidgetRemover(WidgetRepository widgetRepository,
			@Qualifier("delegatingStateContentRemover") WidgetContentRemover widgetContentRemover) {
		this.widgetRepository = widgetRepository;
		this.widgetContentRemover = widgetContentRemover;
	}

	@Override
	public void remove(Project project) {
		List<Widget> widgets = widgetRepository.findAllByProjectIdAndWidgetTypeIn(project.getId(),
				Collections.singletonList(WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType())
		);
		widgets.forEach(widgetContentRemover::removeContent);
	}
}
