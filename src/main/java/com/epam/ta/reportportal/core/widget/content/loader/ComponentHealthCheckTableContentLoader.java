package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.core.widget.content.MaterializedLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.HealthCheckTableResolverStrategy;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ComponentHealthCheckTableContentLoader implements MaterializedLoadContentStrategy {

	private final HealthCheckTableResolverStrategy healthCheckTableResolverStrategy;

	@Autowired
	public ComponentHealthCheckTableContentLoader(HealthCheckTableResolverStrategy healthCheckTableResolverStrategy) {
		this.healthCheckTableResolverStrategy = healthCheckTableResolverStrategy;
	}

	@Override
	public Map<String, Object> loadContent(Widget widget, String[] attributes, Map<String, String> params) {
		return healthCheckTableResolverStrategy.resolveContent(widget, attributes, params);
	}

}
