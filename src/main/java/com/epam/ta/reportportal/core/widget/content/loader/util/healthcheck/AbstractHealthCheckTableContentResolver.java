package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractHealthCheckTableContentResolver implements HealthCheckTableContentResolver {

	public static final Integer MAX_LEVEL_NUMBER = 10;

	protected abstract Map<String, Object> getContent(Widget widget, List<String> attributeKeys, List<String> attributeValues);

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, Object> loadContent(Widget widget, String[] attributes, Map<String, String> params) {

		List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widget.getWidgetOptions());
		List<String> attributeValues = ofNullable(attributes).map(Arrays::asList).orElseGet(Collections::emptyList);

		return getContent(widget, attributeKeys, attributeValues);

	}

}
