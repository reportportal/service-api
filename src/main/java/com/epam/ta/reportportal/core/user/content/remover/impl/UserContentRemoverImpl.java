package com.epam.ta.reportportal.core.user.content.remover.impl;

import com.epam.ta.reportportal.core.user.content.remover.UserContentRemover;
import com.epam.ta.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class UserContentRemoverImpl implements UserContentRemover {

	private final WidgetRepository widgetRepository;
	private final List<WidgetContentRemover> widgetContentRemovers;

	public UserContentRemoverImpl(WidgetRepository widgetRepository, List<WidgetContentRemover> widgetContentRemovers) {
		this.widgetRepository = widgetRepository;
		this.widgetContentRemovers = widgetContentRemovers;
	}

	@Override
	public void removeContent(User user) {
		List<Widget> widgets = widgetRepository.findAllByOwnerAndWidgetTypeIn(user.getLogin(),
				Collections.singletonList(WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType())
		);
		widgets.forEach(w -> widgetContentRemovers.forEach(remover -> remover.removeContent(w)));
	}
}
