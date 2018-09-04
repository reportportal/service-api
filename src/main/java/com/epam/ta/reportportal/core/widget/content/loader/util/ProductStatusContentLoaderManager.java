package com.epam.ta.reportportal.core.widget.content.loader.util;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.ProductStatusContentLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductStatusContentLoaderManager implements LoadContentStrategy {

	private final Map<String, ProductStatusContentLoader> productStatusContentLoader;

	@Autowired
	public ProductStatusContentLoaderManager(
			@Qualifier("productStatusContentLoader") Map<String, ProductStatusContentLoader> productStatusContentLoader) {
		this.productStatusContentLoader = productStatusContentLoader;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, Map<String, String> widgetOptions,
			int limit) {

		return productStatusContentLoader.get(widgetOptions.get("strategy"))
				.loadContent(contentFields, filterSortMapping, widgetOptions, limit);
	}
}
