package com.epam.ta.reportportal.core.widget.content.loader.materialized.handler;

import com.epam.ta.reportportal.core.events.widget.GenerateWidgetViewEvent;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Collections.emptyMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatedMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

	private final WidgetRepository widgetRepository;
	protected ApplicationEventPublisher eventPublisher;

	public CreatedMaterializedWidgetStateHandler(WidgetRepository widgetRepository,
			@Qualifier("webApplicationContext") ApplicationEventPublisher eventPublisher) {
		this.widgetRepository = widgetRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public Map<String, Object> handleWidgetState(Widget widget, MultiValueMap<String, String> params) {
		widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.RENDERING.getValue()).get());
		eventPublisher.publishEvent(new GenerateWidgetViewEvent(widget.getId(), params));
		return emptyMap();
	}

}
