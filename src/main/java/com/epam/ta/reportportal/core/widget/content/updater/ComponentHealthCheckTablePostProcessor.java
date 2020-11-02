package com.epam.ta.reportportal.core.widget.content.updater;

import com.epam.ta.reportportal.core.widget.content.updater.validator.WidgetValidator;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class ComponentHealthCheckTablePostProcessor implements WidgetPostProcessor {

	private final WidgetValidator componentHealthCheckTableValidator;
	private final WidgetUpdater materializedWidgetStateUpdater;

	@Autowired
	public ComponentHealthCheckTablePostProcessor(WidgetValidator componentHealthCheckTableValidator,
			WidgetUpdater materializedWidgetStateUpdater) {
		this.componentHealthCheckTableValidator = componentHealthCheckTableValidator;
		this.materializedWidgetStateUpdater = materializedWidgetStateUpdater;
	}

	@Override
	public boolean supports(Widget widget) {
		return WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType().equalsIgnoreCase(widget.getWidgetType());
	}

	@Override
	public void postProcess(Widget widget) {
		if (supports(widget)) {
			componentHealthCheckTableValidator.validate(widget);
			materializedWidgetStateUpdater.update(widget);
		}
	}
}
