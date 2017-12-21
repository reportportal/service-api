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

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * Strategy definition interface for loading widget content.
 *
 * @author Aliaksei_Makayed
 */
public interface IContentLoadingStrategy {

	/**
	 * Load content. Result map should look like
	 * {@code Map<String, List<ChartObject>>}.<br>
	 * Meaning of returning map content should depend on interface
	 * implementation.
	 *
	 * @param filter         Filter details
	 * @param sorting        Sorting details
	 * @param quantity       Count of items to be loaded
	 * @param contentFields  Fields to be loaded
	 * @param metaDataFields MetaData fields list
	 * @param widgetOptions  Options
	 * @return Chart Data
	 */
	Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity, List<String> contentFields,
			List<String> metaDataFields, Map<String, List<String>> widgetOptions);

}