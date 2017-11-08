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

import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.core.widget.content.WidgetDataTypes;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

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

	public static final Map<GadgetTypes, Class<?>> withoutFilter = ImmutableMap.<GadgetTypes, Class<?>>builder().put(
			GadgetTypes.PRODUCT_STATUS, Launch.class)
			.put(GadgetTypes.ACTIVITY, Activity.class)
			.put(GadgetTypes.PASSING_RATE_PER_LAUNCH, Launch.class)
			.put(GadgetTypes.MOST_FAILED_TEST_CASES, TestItem.class)
			.put(GadgetTypes.FLAKY_TEST_CASES, TestItem.class)
			.build();

	private WidgetUtils() {
		//static only
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
		fields.forEach(field -> expect(criteriaMap.getCriteriaHolderUnchecked(field).isPresent(), equalTo(true)).verify(errorType,
				formattedSupplier("Field '{}' cannot be used for calculating data for widget.", field)
		));
	}

	public static void validateWidgetDataType(String type, ErrorType errorType) {
		expect(WidgetDataTypes.findByName(type).isPresent(), equalTo(true)).verify(errorType, formattedSupplier(
				"Unknown widget data type: '{}'. " + "Possible data types: line_chart, bar_chart, column_chart, combine_pie_chart, table",
				type
		));
	}

	public static void validateGadgetType(String gadget, ErrorType errorType) {
		expect(GadgetTypes.findByName(gadget).isPresent(), equalTo(true)).verify(errorType,
				formattedSupplier("Unknown gadget type: '{}'.", gadget)
		);
	}

	/**
	 * Check is applying filter exists in database.
	 *
	 * @param filterID
	 */
	public static void checkApplyingFilter(UserFilter filter, String filterID, String userName) {
		expect(filter, notNull()).verify(USER_FILTER_NOT_FOUND, filterID, userName);
		expect(filter.isLink(), equalTo(false)).verify(UNABLE_TO_CREATE_WIDGET, "Cannot create widget based on a link.");
	}

	public static void checkUniqueName(String newWidgetName, List<Widget> existingWidgets) {
		if (null != existingWidgets) {
			existingWidgets.forEach(
					existingWidget -> expect(existingWidget.getName().equals(newWidgetName), equalTo(false)).verify(RESOURCE_ALREADY_EXISTS,
							newWidgetName
					));
		}
	}

	/**
	 * Used by trend charts to reorder results for trend representation.
	 *
	 * @param sort Trend Chart sorting
	 * @return true if need to be reverted
	 */
	public static boolean needRevert(Sort sort) {
		if (sort == null) {
			return false;
		}
		String property = sort.iterator().next().getProperty();
		Sort.Order orderFor = sort.getOrderFor(property);
		return property.equalsIgnoreCase(START_TIME) && orderFor.isDescending();
	}
}