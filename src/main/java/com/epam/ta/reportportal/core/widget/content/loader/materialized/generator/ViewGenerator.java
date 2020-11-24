package com.epam.ta.reportportal.core.widget.content.loader.materialized.generator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ViewGenerator {

	void generate(boolean refresh, String viewName, Widget widget, Filter launchesFilter, Sort launchesSort,
			MultiValueMap<String, String> params);
}
