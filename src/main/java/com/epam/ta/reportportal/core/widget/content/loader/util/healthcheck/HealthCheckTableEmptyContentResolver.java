package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class HealthCheckTableEmptyContentResolver extends AbstractHealthCheckTableContentResolver {

	@Override
	protected Map<String, Object> getContent(Widget widget, List<String> attributeKeys, List<String> attributeValues) {
		return Collections.emptyMap();
	}
}
