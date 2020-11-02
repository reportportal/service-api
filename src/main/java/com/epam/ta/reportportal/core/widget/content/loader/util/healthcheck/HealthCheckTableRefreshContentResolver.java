package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.core.events.widget.GenerateComponentHealthCheckTableEvent;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service(value = "healthCheckTableRefreshContentResolver")
public class HealthCheckTableRefreshContentResolver extends HealthCheckTableCreatedContentResolver {

	@Autowired
	public HealthCheckTableRefreshContentResolver(WidgetRepository widgetRepository,
			@Qualifier("webApplicationContext") ApplicationEventPublisher eventPublisher) {
		super(widgetRepository, eventPublisher);
	}

	@Override
	protected void generateContent(Widget widget, List<String> attributeKeys) {
		eventPublisher.publishEvent(new GenerateComponentHealthCheckTableEvent(widget.getId(), true, attributeKeys));
	}
}
