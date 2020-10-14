package com.epam.ta.reportportal.core.widget.content.remover;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class ComponentHealthCheckTableRemover implements WidgetContentRemover {

	private final WidgetContentRepository widgetContentRepository;

	public ComponentHealthCheckTableRemover(WidgetContentRepository widgetContentRepository) {
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public void removeContent(Widget widget) {
		if (supports(widget)) {
			validateState(widget.getWidgetOptions());
			ofNullable(WidgetOptionUtil.getValueByKey(VIEW_NAME,
					widget.getWidgetOptions()
			)).ifPresent(widgetContentRepository::removeWidgetView);
		}
	}

	@Override
	public boolean supports(Widget widget) {
		return WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType().equalsIgnoreCase(widget.getWidgetType());
	}

	private void validateState(WidgetOptions widgetOptions) {
		WidgetState widgetState = ofNullable(WidgetOptionUtil.getValueByKey(STATE, widgetOptions)).flatMap(WidgetState::findByName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_UPDATE_WIDGET_REQUEST, "Widget state not provided"));
		BusinessRule.expect(widgetState, it -> !WidgetState.RENDERING.equals(it))
				.verify(ErrorType.BAD_UPDATE_WIDGET_REQUEST, "Unable to remove widget in 'rendering' state");
	}
}
