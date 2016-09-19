/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.StatisticsDocumentHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Lists;

/**
 * ContentLoader implementation for <b>Investigated Gadget</b>.<br>
 * Content represents investigated part of issues in percents to <br>
 * all 'To investigate' items.
 * 
 * @author Andrei_Ramanchuk
 * 
 */
@Service("ChartInvestigatedContentLoader")
public class ChartInvestigatedContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(Filter filter, Sort sorting, int quantity, List<String> contentFields,
			List<String> metaDataFields, Map<String, List<String>> options) {
		BusinessRule.expect(metaDataFields == null || metaDataFields.isEmpty(), Predicates.equalTo(Boolean.FALSE))
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Metadata fields should exist for providing content for 'column chart'.");
		List<String> allFields = Lists.newArrayList(contentFields);
		List<String> xAxis = metaDataFields;
		allFields.addAll(xAxis);
		StatisticsDocumentHandler handler = new StatisticsDocumentHandler(contentFields, xAxis);
		String collectionName = getCollectionName(filter.getTarget());

		// here can be used any repository which extends ReposrtPortalRepository
		launchRepository.loadWithCallback(filter, sorting, quantity, allFields, handler, collectionName);
		if ((options.get("timeline") != null) && (Period.findByName(options.get("timeline").get(0)) != null)) {
			return convertResult(groupByDate(handler.getResult(), Period.findByName(options.get("timeline").get(0))));
		}
		Map<String, List<ChartObject>> result = new HashMap<>();
		result.put("result", handler.getResult());
		return convertResult(result);
	}

	/**
	 * Convert database query result to chart data
	 * 
	 * @param initial
	 * @return
	 */
	private Map<String, List<ChartObject>> convertResult(Map<String, List<ChartObject>> initial) {
		if (initial.size() == 0)
			return new HashMap<>();

		for (Map.Entry<String, List<ChartObject>> entry : initial.entrySet()) {
			entry.getValue().stream().forEach(chart -> chart = this.getInvestigationStatistic(chart));
		}
		return initial;
	}

	private ChartObject getInvestigationStatistic(ChartObject init) {
		DecimalFormat formatter = new DecimalFormat("###.##");
		final String INVESTIGATED = "investigated";
		final String TO_INVESTIGATE = "to_investigate";
		Map<String, String> additionalValues = new HashMap<>();
		double investigated = 0;
		double toInvestigate = 0;
		for (String key : init.getValues().keySet()) {
			if (Arrays.asList(getIssueStatFields()).contains(key))
				investigated = investigated + Double.valueOf(init.getValues().get(key));
			if (key.equalsIgnoreCase(getToInvestigateFieldName()))
				toInvestigate = Double.valueOf(init.getValues().get(key));
		}
		/* DON'T USE COMPARE OPERANDS WITH DOUBLE!!! */
		if ((investigated + toInvestigate) > 0) {
			double investigatedPercent = (investigated / (investigated + toInvestigate)) * 100;
			double toInvestigatePercent = 100 - investigatedPercent;
			additionalValues.put(INVESTIGATED, formatter.format(investigatedPercent));
			additionalValues.put(TO_INVESTIGATE, formatter.format(toInvestigatePercent));
		} else {
			additionalValues.put(INVESTIGATED, formatter.format(0));
			additionalValues.put(TO_INVESTIGATE, formatter.format(0));
		}
		init.setValues(additionalValues);
		return init;
	}
}