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

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_WIDGET_CONTENT;
import static java.util.stream.Collectors.toList;

import java.util.*;

import javax.annotation.Resource;

import com.epam.ta.reportportal.database.search.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Lists;

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
	@Resource(name = "contentLoaders")
	private Map<GadgetTypes, IContentLoadingStrategy> contentLoadersMap;

	/**
	 * Load content according input parameters
	 *
	 * @param filter
	 * @param selectionOptions
	 * @param options
	 */
	public Map<String, List<ChartObject>> getChartContent(Filter filter, SelectionOptions selectionOptions, ContentOptions options) {

		boolean revertResult = false;

		Class<?> type = filter.getTarget();
		CriteriaMap<?> criteriaMap = criteriaMapFactory.getCriteriaMap(type);

		// all data fields names should be converted to db style
		List<String> contentFields = transformToDBStyle(criteriaMap, options.getContentFields());
		List<String> metaDataFields = transformToDBStyle(criteriaMap, options.getMetadataFields());

		boolean isAscSort = selectionOptions.isAsc();

		/*
		 * Dirty handler of not full output during ASC start_time sorting. In
		 * 'a- lot-of-results' case users got last results truncated by page
		 * limitation.
		 */
		Sort sort;
		String sortingColumnName = selectionOptions.getSortingColumnName();
		if ("start_time".equalsIgnoreCase(sortingColumnName) && isAscSort) {
			sort = new Sort(Sort.Direction.DESC, criteriaMap.getCriteriaHolder(sortingColumnName).getQueryCriteria());
			revertResult = true;
		} else {
			sort = new Sort(isAscSort ? Sort.Direction.ASC : Sort.Direction.DESC,
					criteriaMap.getCriteriaHolder(sortingColumnName).getQueryCriteria());
		}

		Map<String, List<ChartObject>> result;

		IContentLoadingStrategy loadingStrategy = contentLoadersMap.get(GadgetTypes.findByName(options.getGadgetType()).get());
		expect(loadingStrategy, notNull()).verify(UNABLE_LOAD_WIDGET_CONTENT,
				Suppliers.formattedSupplier("Unknown gadget type: '{}'.", options.getGadgetType()));
		Map<String, List<String>> widgetOptions = null == options.getWidgetOptions() ? new HashMap<>() : options.getWidgetOptions();
		result = loadingStrategy.loadContent(filter, sort, options.getItemsCount(), contentFields, metaDataFields, widgetOptions);

		if (null != options.getContentFields()) {
			result = transformToFilterStyle(criteriaMap, result, options.getContentFields());
			result = transformNamesForUI(result);
		}
		/*
		 * Reordering of results for user-friendly UI output. NOTE: Probably it
		 * will be moved to 'transformation' method.
		 */
		// TODO: move transformations in one common method
		// TODO: replace dirty-hack with specific gadget data freeze
		if (!revertResult || options.getGadgetType().equalsIgnoreCase(GadgetTypes.CASES_TREND.getType())) {
			result = mapRevert(result);
		}
		return result;
	}

	/**
	 * Transform chart data fields names to ui known names using criteria
	 * holder.
	 */
	private Map<String, List<ChartObject>> transformToFilterStyle(CriteriaMap<?> criteriaMap, Map<String, List<ChartObject>> input,
			List<String> chartFields) {
		Map<String, List<ChartObject>> result = new LinkedHashMap<>();

		for (Map.Entry<String, List<ChartObject>> entry : input.entrySet()) {
			boolean isConverted = false;
			List<ChartObject> data = entry.getValue();
			for (String field : chartFields) {

				String queryCriteria = criteriaMap.getCriteriaHolder(field).getQueryCriteria();
				if (queryCriteria.equals(entry.getKey())) {
					result.put(criteriaMap.getCriteriaHolder(queryCriteria).getFilterCriteria(), data);
					isConverted = true;
					break;
				}
			}
			if (!isConverted)
				result.put(entry.getKey(), data);
		}
		return result;
	}

	/**
	 * Transformer of '.' separator names into '$' separator (by UI request)
	 *
	 * @param input
	 * @return
	 */
	private Map<String, List<ChartObject>> transformNamesForUI(Map<String, List<ChartObject>> input) {
		// TODO RECREATE with Java 8 streaming!
		for (Map.Entry<String, List<ChartObject>> entry : input.entrySet()) {
			for (ChartObject exist : entry.getValue()) {
				Map<String, String> values = new HashMap<>();
				for (String key : exist.getValues().keySet()) {
					String keyValue = exist.getValues().get(key);
					values.put(key.replaceAll("\\.", "\\$"), keyValue);
				}
				exist.setValues(values);
			}
		}
		return input;
	}

	/**
	 * Transform chart data fields names to database known names using criteria
	 * holder.
	 */
	public static List<String> transformToDBStyle(CriteriaMap<?> criteriaMap, List<String> chartFields) {
		if (chartFields == null)
			return new ArrayList<>();
		return chartFields.stream().map(it -> {
			return criteriaMap.getCriteriaHolder(it).getQueryCriteria();
					//+ ((filterCriteria.getExtension() != null) ? "." + filterCriteria.getExtension() : "");
		}).collect(toList());
	}

	/**
	 * Transformation function for avoidance of truncated results by filter
	 *
	 * @param input
	 *            - callback output result
	 * @return - transformed Map with reverse ordered elements
	 */
	private Map<String, List<ChartObject>> mapRevert(Map<String, List<ChartObject>> input) {
		Map<String, List<ChartObject>> result = new LinkedHashMap<>();
		for (Map.Entry<String, List<ChartObject>> entry : input.entrySet()) {
			List<ChartObject> newOrder = Lists.newArrayList();
			List<ChartObject> data = entry.getValue();

			for (int i = (data.size() - 1); i >= 0; i--)
				newOrder.add(data.get(i));

			result.put(entry.getKey(), newOrder);
		}
		return result;
	}
}