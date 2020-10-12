package com.epam.ta.reportportal.core.widget.content.loader.materialized;

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
@Service(value = "readyMaterializedContentLoader")
public class ReadyMaterializedContentLoaderDelegate implements MaterializedContentLoader {

	private final MaterializedContentLoader refreshContentLoaderDelegate;
	private final Map<WidgetType, MaterializedContentLoader> readyMaterializedContentLoaderMapping;

	public ReadyMaterializedContentLoaderDelegate(
			@Qualifier("createdMaterializedContentLoader") MaterializedContentLoader refreshContentLoaderDelegate,
			@Qualifier("readyMaterializedContentLoaderMapping")
					Map<WidgetType, MaterializedContentLoader> readyMaterializedContentLoaderMapping) {
		this.refreshContentLoaderDelegate = refreshContentLoaderDelegate;
		this.readyMaterializedContentLoaderMapping = readyMaterializedContentLoaderMapping;
	}

	@Override
	public Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params) {

		if (BooleanUtils.toBoolean(params.getFirst(REFRESH))) {
			return refreshContentLoaderDelegate.loadContent(widget, params);
		}

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
						formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
				));

		return ofNullable(readyMaterializedContentLoaderMapping.get(widgetType)).map(loader -> loader.loadContent(widget, params))
				.orElseGet(Collections::emptyMap);
	}
}
