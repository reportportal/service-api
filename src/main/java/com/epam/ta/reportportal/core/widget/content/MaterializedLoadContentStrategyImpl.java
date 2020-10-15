package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class MaterializedLoadContentStrategyImpl implements MaterializedLoadContentStrategy {

	private final Map<WidgetState, MaterializedWidgetStateHandler> widgetStateHandlerMapping;

	@Autowired
	public MaterializedLoadContentStrategyImpl(@Qualifier("widgetStateHandlerMapping") Map<WidgetState, MaterializedWidgetStateHandler> widgetStateHandlerMapping) {
		this.widgetStateHandlerMapping = widgetStateHandlerMapping;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params) {

		WidgetState widgetState = ofNullable(WidgetOptionUtil.getValueByKey(STATE,
				widget.getWidgetOptions()
		)).flatMap(WidgetState::findByName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Widget state not provided"));

		return widgetStateHandlerMapping.get(widgetState).handleWidgetState(widget, params);
	}
}
