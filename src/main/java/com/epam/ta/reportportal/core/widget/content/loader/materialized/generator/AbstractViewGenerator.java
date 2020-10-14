package com.epam.ta.reportportal.core.widget.content.loader.materialized.generator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractViewGenerator implements ViewGenerator {

	public static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewGenerator.class);

	private static final String LAST_REFRESH = "lastRefresh";

	private final WidgetRepository widgetRepository;

	public AbstractViewGenerator(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	protected abstract void generateView(boolean refresh, String viewName, Widget widget, Filter launchesFilter, Sort launchesSort,
			MultiValueMap<String, String> params);

	@Transactional
	public void generate(boolean refresh, String viewName, Widget widget, Filter launchesFilter, Sort launchesSort,
			MultiValueMap<String, String> params) {
		try {
			LOGGER.debug("Widget {} - {}. Generation started", widget.getWidgetType(), widget.getId());
			generateView(refresh, viewName, widget, launchesFilter, launchesSort, params);
			LOGGER.debug("Widget {} - {}. Generation finished", widget.getWidgetType(), widget.getId());
			widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.READY.getValue())
					.addOption(VIEW_NAME, viewName)
					.addOption(LAST_REFRESH, Date.from(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant()))
					.get());
			LOGGER.debug("Widget {} - {}. State updated to: {}",
					widget.getWidgetType(),
					widget.getId(),
					WidgetOptionUtil.getValueByKey(STATE, widget.getWidgetOptions())
			);
		} catch (Exception exc) {
			LOGGER.error("Error during view creation: " + exc.getMessage());
			widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.FAILED.getValue()).get());
			LOGGER.error("Generation failed. Widget {} - {}. State updated to: {}",
					widget.getWidgetType(),
					widget.getId(),
					WidgetOptionUtil.getValueByKey(STATE, widget.getWidgetOptions())
			);
		}

	}

}
