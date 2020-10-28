package com.epam.ta.reportportal.core.widget.content.loader.materialized;

import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface MaterializedWidgetContentLoader {

	Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params);
}
