package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.util;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LATEST_OPTION;
import static com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.HealthCheckTableReadyContentResolver.VIEW_NAME;
import static com.epam.ta.reportportal.core.widget.content.updater.ComponentHealthCheckTableUpdater.STATE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class HealthCheckTableGenerator {

	public static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckTableGenerator.class);

	private final WidgetContentRepository widgetContentRepository;
	private final WidgetRepository widgetRepository;

	public HealthCheckTableGenerator(WidgetContentRepository widgetContentRepository, WidgetRepository widgetRepository) {
		this.widgetContentRepository = widgetContentRepository;
		this.widgetRepository = widgetRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void generate(HealthCheckTableInitParams initParams, Widget widget, Filter launchesFilter, Sort launchesSort) {
		boolean generated;
		try {
			widgetContentRepository.generateComponentHealthCheckTable(initParams,
					launchesFilter,
					launchesSort,
					widget.getItemsCount(),
					WidgetOptionUtil.getBooleanByKey(LATEST_OPTION, widget.getWidgetOptions())
			);
			generated = true;
		} catch (Exception exc) {
			LOGGER.error("Error during view creation: " + exc.getMessage());
			generated = false;
		}

		if (generated) {
			widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.READY.getValue())
					.addOption(VIEW_NAME, initParams.getViewName())
					.get());
		} else {
			widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.FAILED.getValue()).get());
		}

	}
}
