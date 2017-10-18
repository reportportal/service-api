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

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
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

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for 'Non-Passed test-cases trend chart' widget content
 * calculation
 *
 * @author Andrei_Ramanchuk
 */
@Service("NotPassedTestsContentLoader")
public class NotPassedTestsContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	private final static String NOT_PASSED_PERCENT = "% (Failed+Skipped)/Total";

	@Autowired
	private LaunchRepository launchRepository;

	@SuppressFBWarnings("NP_NULL_PARAM_DEREF")
	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {
		BusinessRule.expect(metaDataFields == null || metaDataFields.isEmpty(), Predicates.equalTo(Boolean.FALSE))
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Metadata fields should exist for providing content for 'column chart'.");
		List<String> allFields = ImmutableList.<String>builder().addAll(contentFields).addAll(metaDataFields).build();
		StatisticsDocumentHandler handler = new StatisticsDocumentHandler(contentFields, metaDataFields);
		String collectionName = getCollectionName(filter.getTarget());

		// here can be used any repository which extends ReportPortalRepository
		if (options.containsKey(LATEST_MODE)) {
			launchRepository.findLatestWithCallback(filter, sorting, allFields, quantity, handler);
		} else {
			launchRepository.loadWithCallback(filter, sorting, quantity, allFields, handler, collectionName);
		}
		return this.convertResult(handler, sorting);
	}

	/**
	 * Convert database query result to chart data
	 *
	 * @param handler
	 * @return
	 */
	private Map<String, List<ChartObject>> convertResult(StatisticsDocumentHandler handler, Sort sort) {
		DecimalFormat formatter = new DecimalFormat("###.##");

		// Empty result if callback return empty map
		List<ChartObject> objects = handler.getResult();
		if (objects.isEmpty()) {
			return Collections.emptyMap();
		}
		if (WidgetUtils.needRevert(sort)) {
			Collections.reverse(objects);
		}
		for (ChartObject object : objects) {
			Map<String, String> values = new HashMap<>();
			double failed = Integer.parseInt(object.getValues().get(getFailedFieldName()));
			double skipped = Integer.parseInt(object.getValues().get(getSkippedFieldName()));
			double total = Integer.parseInt(object.getValues().get(getTotalFieldName()));

			if (total > 0) {
				double percent = (failed + skipped) / total * 100;
				values.put(NOT_PASSED_PERCENT, formatter.format(percent));
			} else {
				values.put(NOT_PASSED_PERCENT, "0");
			}
			object.setValues(values);
		}
		return Collections.singletonMap(RESULT, objects);
	}
}
