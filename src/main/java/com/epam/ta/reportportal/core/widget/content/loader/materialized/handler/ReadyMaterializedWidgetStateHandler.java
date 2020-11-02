package com.epam.ta.reportportal.core.widget.content.loader.materialized.handler;

import com.epam.ta.reportportal.core.widget.content.loader.materialized.MaterializedWidgetContentLoader;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ReadyMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

	private final MaterializedWidgetStateHandler refreshWidgetStateHandler;
	private final Map<WidgetType, MaterializedWidgetContentLoader> materializedWidgetContentLoaderMapping;

	public ReadyMaterializedWidgetStateHandler(
			@Qualifier("createdMaterializedWidgetStateHandler") MaterializedWidgetStateHandler refreshWidgetStateHandler,
			@Qualifier("materializedWidgetContentLoaderMapping")
					Map<WidgetType, MaterializedWidgetContentLoader> materializedWidgetContentLoaderMapping) {
		this.refreshWidgetStateHandler = refreshWidgetStateHandler;
		this.materializedWidgetContentLoaderMapping = materializedWidgetContentLoaderMapping;
	}

	@Override
	public Map<String, Object> handleWidgetState(Widget widget, MultiValueMap<String, String> params) {

		if (BooleanUtils.toBoolean(params.getFirst(REFRESH))) {
			return refreshWidgetStateHandler.handleWidgetState(widget, params);
		}

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
						formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
				));

		return ofNullable(materializedWidgetContentLoaderMapping.get(widgetType)).map(loader -> loader.loadContent(widget, params))
				.orElseGet(Collections::emptyMap);
	}
}
