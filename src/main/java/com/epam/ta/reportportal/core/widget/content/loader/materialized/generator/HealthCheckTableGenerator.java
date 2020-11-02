package com.epam.ta.reportportal.core.widget.content.loader.materialized.generator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LATEST_OPTION;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class HealthCheckTableGenerator extends AbstractViewGenerator {

	public static final String CUSTOM_COLUMN = "customColumn";

	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public HealthCheckTableGenerator(WidgetRepository widgetRepository, WidgetContentRepository widgetContentRepository) {
		super(widgetRepository);
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	protected void generateView(boolean refresh, String viewName, Widget widget, Filter launchesFilter, Sort launchesSort,
			MultiValueMap<String, String> params) {
		widgetContentRepository.generateComponentHealthCheckTable(refresh,
				getInitParams(widget, viewName),
				launchesFilter,
				launchesSort,
				widget.getItemsCount(),
				WidgetOptionUtil.getBooleanByKey(LATEST_OPTION, widget.getWidgetOptions())
		);
	}

	private HealthCheckTableInitParams getInitParams(Widget widget, String viewName) {
		List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widget.getWidgetOptions());
		return ofNullable(WidgetOptionUtil.getValueByKey(CUSTOM_COLUMN, widget.getWidgetOptions())).map(custom -> HealthCheckTableInitParams
				.of(viewName, attributeKeys, custom)).orElseGet(() -> HealthCheckTableInitParams.of(viewName, attributeKeys));
	}
}
