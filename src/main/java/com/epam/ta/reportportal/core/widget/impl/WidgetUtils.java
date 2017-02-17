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

package com.epam.ta.reportportal.core.widget.impl;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.RESOURCE_ALREADY_EXISTS;

import java.util.List;

import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.core.widget.content.WidgetDataTypes;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.CriteriaHolder;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.FilterCriteria;
import com.epam.ta.reportportal.database.search.QueryBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;

/**
 * Widget's related utils
 * 
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
public class WidgetUtils {

	public static final String NAME = "name";
	public static final String NUMBER = "number";
	public static final String START_TIME = "start_time";
	public static final String USER = "user";

	private WidgetUtils() {

	}

	/**
	 * Check is widget's data fields names can be converted to DB style.
	 * 
	 * @param fields
	 * @param criteriaMap
	 * @param errorType
	 */
	public static void validateFields(Iterable<String> fields, CriteriaMap<?> criteriaMap, ErrorType errorType) {
		if (fields == null || criteriaMap == null || errorType == null) {
			return;
		}
		for (String field : fields) {
			expect(criteriaMap.getCriteriaHolderUnchecked(field).isPresent(), equalTo(true))
					.verify(errorType, formattedSupplier("Field '{}' cannot be used for calculating data for widget.", field));
		}
	}

	public static void validateWidgetDataType(String type, ErrorType errorType) {
		expect(WidgetDataTypes.findByName(type).isPresent(), equalTo(true)).verify(errorType,
				formattedSupplier(
						"Unknown widget data type: '{}'. " + "Possible data types: line_chart, bar_chart, column_chart, pie_chart, table",
						type));
	}

	public static void validateGadgetType(String gadget, ErrorType errorType) {
		expect(GadgetTypes.findByName(gadget).isPresent(), equalTo(true)).verify(errorType,
				formattedSupplier("Unknown gadget type: '{}'.", gadget));
	}

	public static void checkUniqueName(String newWidgetName, List<Widget> existingWidgets) {
		if (null != existingWidgets) {
			for (Widget existingWidget : existingWidgets) {
				expect(existingWidget.getName().equals(newWidgetName), equalTo(false))
						.verify(RESOURCE_ALREADY_EXISTS, newWidgetName);
			}
		}
	}
}