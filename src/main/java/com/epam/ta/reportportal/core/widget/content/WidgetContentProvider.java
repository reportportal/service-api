/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_WIDGET_CONTENT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Widget content strategy context.<br>
 * Content represented as {@code Map<String, Map<String, List<AxisObject>>>}
 * depending on {@link ContentOptions}'s content type.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class WidgetContentProvider {

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

	/*
	 * key - widget content; type value - content loader
	 */
	@Autowired
	@Qualifier("contentLoader")
	private Map<GadgetTypes, IContentLoadingStrategy> contentLoader;

	public static final Function<String, String> TO_UI_STYLE = db -> db.replace('.', '$');
	public static final Function<String, String> TO_DB_STYLE = model -> model.replace('$', '.');

	/**
	 * Load content according input parameters
	 *
	 * @param filter
	 * @param selectionOptions
	 * @param options
	 */
	public Map<String, List<ChartObject>> getChartContent(String projectName, Filter filter, SelectionOptions selectionOptions,
			ContentOptions options) {

		Class<?> type = filter.getTarget();
		CriteriaMap<?> criteriaMap = criteriaMapFactory.getCriteriaMap(type);

		// all data fields names should be converted to db style
		List<String> contentFields = transformToDBStyle(criteriaMap, options.getContentFields());
		List<String> metaDataFields = transformToDBStyle(criteriaMap, options.getMetadataFields());

		List<Sort.Order> orders = selectionOptions.getOrders()
				.stream()
				.map(order -> new Sort.Order(order.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
						criteriaMap.getCriteriaHolder(order.getSortingColumnName()).getQueryCriteria()
				))
				.collect(toList());
		Sort sort = new Sort(orders);

		Map<String, List<ChartObject>> result;
		IContentLoadingStrategy loadingStrategy = contentLoader.get(GadgetTypes.findByName(options.getGadgetType()).get());
		expect(loadingStrategy, notNull()).verify(UNABLE_LOAD_WIDGET_CONTENT,
				Suppliers.formattedSupplier("Unknown gadget type: '{}'.", options.getGadgetType())
		);
		Map<String, List<String>> widgetOptions = null == options.getWidgetOptions() ? new HashMap<>() : options.getWidgetOptions();
		int itemsCount = options.getItemsCount();
		result = loadingStrategy.loadContent(projectName, filter, sort, itemsCount, contentFields, metaDataFields, widgetOptions);
		if (null != options.getContentFields()) {
			result = transformNamesForUI(criteriaMap, options.getContentFields(), result);
		}
		return result;
	}

	/**
	 * Transformer of '.' separator names into '$' separator (by UI request)
	 *
	 * @param input
	 * @return
	 */
	private Map<String, List<ChartObject>> transformNamesForUI(CriteriaMap<?> criteriaMap, List<String> chartFields,
			Map<String, List<ChartObject>> input) {

		Map<String, String> reversedCriteriaMap = chartFields.stream()
				.collect(toMap(field -> criteriaMap.getCriteriaHolder(field).getQueryCriteria(), field -> field));

		input.entrySet().stream().flatMap(it -> it.getValue().stream()).forEach(chartObject -> {
			Map<String, String> values = new LinkedHashMap<>();
			chartObject.getValues().keySet().forEach(key -> {
				String value = chartObject.getValues().get(key);

				// keys could not be in db style, so should be reverted
				String queryCriteria = reversedCriteriaMap.get(TO_DB_STYLE.apply(key));
				if (queryCriteria != null) {
					values.put(queryCriteria, value);
				} else {
					values.put(key, value);
				}
			});
			chartObject.setValues(values);
		});

		return input;
	}

	/**
	 * Transform chart data fields names to database known names using criteria
	 * holder.
	 */
	public static List<String> transformToDBStyle(CriteriaMap<?> criteriaMap, List<String> chartFields) {
		if (chartFields == null) {
			return new ArrayList<>();
		}
		return chartFields.stream().map(it -> criteriaMap.getCriteriaHolder(it).getQueryCriteria()).collect(toList());
	}

}