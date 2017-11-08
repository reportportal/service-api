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
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.START_TIME;

/**
 * ContentLoader implementation for <b>Different launches comparison chart
 * widget</b>
 *
 * @author Dzmitry_Kavalets
 * @author Andrei_Ramanchuk
 */
@Service("LaunchesComparisonChart")
public class LaunchesComparisonContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	// TODO temporary solution
	private final static Integer QUANTITY = 2;

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {

		if (filter.getTarget().equals(TestItem.class)) {
			return Collections.emptyMap();
		}
		StatisticsDocumentHandler documentHandler = new StatisticsDocumentHandler(contentFields, metaDataFields);
		List<String> allFields = ImmutableList.<String>builder().addAll(contentFields).addAll(metaDataFields).build();

		//fixed sorting
		sorting = new Sort(Sort.Direction.DESC, START_TIME);
		String collectionName = getCollectionName(filter.getTarget());
		launchRepository.loadWithCallback(filter, sorting, QUANTITY, allFields, documentHandler, collectionName);
		List<ChartObject> result = documentHandler.getResult();
		return convertResult(result, sorting);
	}

	private Map<String, List<ChartObject>> convertResult(List<ChartObject> objects, Sort sort) {
		DecimalFormat formatter = new DecimalFormat("###.##");

		if (WidgetUtils.needRevert(sort)) {
			Collections.reverse(objects);
		}

		for (ChartObject object : objects) {
			Map<String, String> values = new HashMap<>();
			/* Total */
			Double totalValue = Double.valueOf(object.getValues().get(getTotalFieldName()));
			values.put(getTotalFieldName(), formatter.format(totalValue));

			/* Failed */
			Double failedItems =
					totalValue.intValue() == 0 ? 0.0 : Double.valueOf(object.getValues().get(getFailedFieldName())) / totalValue * 100;
			values.put(getFailedFieldName(), formatter.format(failedItems));

			/* Skipped */
			Double skippedItems =
					totalValue.intValue() == 0 ? 0.0 : Double.valueOf(object.getValues().get(getSkippedFieldName())) / totalValue * 100;
			values.put(getSkippedFieldName(), formatter.format(skippedItems));

			/* Passed */
			values.put(getPassedFieldName(), formatter.format((totalValue.intValue() == 0) ? 0.0 : 100 - failedItems - skippedItems));

			/* To Investigate */
			int toInvestigateQuantity = Integer.parseInt(object.getValues().get(getToInvestigateFieldName()));

			/* Product Bugs */
			int productBugsQuantity = Integer.parseInt(object.getValues().get(getProductBugFieldName()));

			/* System Issues */
			int systemIssuesQuantity = Integer.parseInt(object.getValues().get(getSystemIssueFieldName()));

			/* Automation Bugs */
			int testBugsQuantity = Integer.parseInt(object.getValues().get(getAutomationBugFieldName()));

			String noDefectValue = object.getValues().get(getNoDefectFieldName());
			int noDefectQuantity = noDefectValue == null ? 0 : Integer.parseInt(noDefectValue);

			int failedQuantity = toInvestigateQuantity + productBugsQuantity + systemIssuesQuantity + testBugsQuantity + noDefectQuantity;
			if (failedQuantity != 0) {
				Double investigatedItems = (double) toInvestigateQuantity / failedQuantity * 100;
				values.put(getToInvestigateFieldName(), formatter.format(investigatedItems));

				Double productBugItems = (double) productBugsQuantity / failedQuantity * 100;
				values.put(getProductBugFieldName(), formatter.format(productBugItems));

				Double systemIssueItems = (double) systemIssuesQuantity / failedQuantity * 100;
				values.put(getSystemIssueFieldName(), formatter.format(systemIssueItems));

				Double noDefectItems = (double) noDefectQuantity / failedQuantity * 100;
				values.put(getNoDefectFieldName(), formatter.format(noDefectItems));

				values.put(
						getAutomationBugFieldName(),
						formatter.format(100 - investigatedItems - productBugItems - systemIssueItems - noDefectItems)
				);
			} else {
				String formatted = formatter.format(0.0);
				values.put(getToInvestigateFieldName(), formatted);
				values.put(getProductBugFieldName(), formatted);
				values.put(getSystemIssueFieldName(), formatted);
				values.put(getAutomationBugFieldName(), formatted);
				values.put(getNoDefectFieldName(), formatted);
			}
			object.setValues(values);
		}
		return Collections.singletonMap(RESULT, objects);
	}
}