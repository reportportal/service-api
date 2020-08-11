package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;

@Component("widgetContentFieldsValidator")
public class WidgetContentFieldsValidator implements WidgetValidator {

	private Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;

	private Map<WidgetType, WidgetValidatorStrategy> widgetValidatorLoader;

	private Map<WidgetType, MultilevelValidatorStrategy> multilevelValidatorLoader;

	@Autowired
	@Qualifier("buildFilterStrategy")
	public void setBuildFilterStrategy(Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping) {
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
	}

	@Autowired
	@Qualifier("widgetValidatorLoader")
	public void setWidgetValidatorLoader(Map<WidgetType, WidgetValidatorStrategy> widgetValidatorLoader) {
		this.widgetValidatorLoader = widgetValidatorLoader;
	}

	@Autowired
	@Qualifier("multilevelValidatorLoader")
	public void setMultilevelValidatorLoader(Map<WidgetType, MultilevelValidatorStrategy> multilevelValidatorLoader) {
		this.multilevelValidatorLoader = multilevelValidatorLoader;
	}

	@Override
	public void validate(Widget widget) {
		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
				));

		if (widgetType.isSupportMultilevelStructure()) {
			multilevelValidatorLoader.get(widgetType)
					.validate(Lists.newArrayList(widget.getContentFields()),
							buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
							widget.getWidgetOptions(),
							null,
							null,
							widget.getItemsCount()
					);
		} else {
			widgetValidatorLoader.get(widgetType)
					.validate(Lists.newArrayList(widget.getContentFields()),
							buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
							widget.getWidgetOptions(),
							widget.getItemsCount()
					);
		}
	}
}
