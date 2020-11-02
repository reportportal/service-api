package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.core.events.widget.GenerateComponentHealthCheckTableEvent;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.updater.ComponentHealthCheckTableUpdater.STATE;
import static java.util.Collections.emptyMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service(value = "healthCheckTableCreatedContentResolver")
public class HealthCheckTableCreatedContentResolver extends AbstractHealthCheckTableContentResolver {

	private final WidgetRepository widgetRepository;
	protected ApplicationEventPublisher eventPublisher;

	public HealthCheckTableCreatedContentResolver(WidgetRepository widgetRepository,
			@Qualifier("webApplicationContext") ApplicationEventPublisher eventPublisher) {
		this.widgetRepository = widgetRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public Map<String, Object> getContent(Widget widget, List<String> attributeKeys, List<String> attributeValues) {

		widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.RENDERING.getValue()).get());

		generateContent(widget, attributeKeys);

		return emptyMap();
	}

	protected void generateContent(Widget widget, List<String> attributeKeys) {
		eventPublisher.publishEvent(new GenerateComponentHealthCheckTableEvent(widget.getId(), false, attributeKeys));
	}

}
