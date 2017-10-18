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

import com.epam.ta.reportportal.core.widget.impl.WidgetUtils;
import com.epam.ta.reportportal.database.StatisticsDocumentHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * ContentLoader implementation for <b>Launch Statistics Gadget</b>.<br>
 * Content represents pie\bar view of launch statistic.
 *
 * @author Andrei_Ramanchuk
 */
@Service("LaunchStatisticsChartContentLoader")
public class LaunchStatisticsChartContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	@Autowired
	private LaunchRepository launchRepository;

	@SuppressFBWarnings("NP_NULL_PARAM_DEREF")
	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {
		expect(metaDataFields == null || metaDataFields.isEmpty(), equalTo(false)).verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
				"Metadata fields should exist for providing content for 'Launch Statistics Chart'."
		);
		List<String> allFields = ImmutableList.<String>builder().addAll(contentFields).addAll(metaDataFields).build();
		StatisticsDocumentHandler handler = new StatisticsDocumentHandler(contentFields, metaDataFields);
		String collectionName = getCollectionName(filter.getTarget());
		// pie charts use only last launch
		quantity = 1;
		// to get last sorting should be DESC in any case
		sorting = new Sort(Sort.Direction.DESC, WidgetUtils.START_TIME);
		launchRepository.loadWithCallback(filter, sorting, quantity, allFields, handler, collectionName);

		Map<String, List<ChartObject>> result = this.convertResult(handler);
		if ((options.get(TIMELINE) != null) && (Period.findByName(options.get(TIMELINE).get(0)) != null)) {
			return groupByDate(handler.getResult(), Period.findByName(options.get(TIMELINE).get(0)));
		}
		return result;
	}

	/**
	 * Convert to chart result view
	 *
	 * @param handler
	 * @return
	 */
	private Map<String, List<ChartObject>> convertResult(StatisticsDocumentHandler handler) {
		Map<String, List<ChartObject>> asResult = new HashMap<>();
		List<ChartObject> initial = handler.getResult();
		final ChartObject chartObject = new ChartObject();
		chartObject.setValues(new HashMap<>());
		asResult.put(RESULT, Collections.singletonList(!initial.isEmpty() ? initial.get(0) : chartObject));
		return asResult;
	}
}
