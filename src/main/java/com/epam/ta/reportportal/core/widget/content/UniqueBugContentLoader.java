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

import com.epam.ta.reportportal.database.UniqueBugDocumentHandler;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of
 * {@link com.epam.ta.reportportal.core.widget.content.IContentLoadingStrategy}
 * for Unique Bug Table
 *
 * @author Dzmitry_Kavalets
 */
@Service("UniqueBugContentLoader")
public class UniqueBugContentLoader implements IContentLoadingStrategy {

	private static final String[] META_DATA_FIELDS = { "launchRef", "submitDate", "submitter", "ticketId", "issue" };
	private static final String COLLECTION_NAME = "testItem";

	@Autowired
	private TestItemRepository testItemRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {

		UniqueBugDocumentHandler uniqueBugDocumentHandler = new UniqueBugDocumentHandler();

		testItemRepository.loadWithCallback(filter, sorting, Integer.MAX_VALUE, Arrays.asList(META_DATA_FIELDS), uniqueBugDocumentHandler,
				COLLECTION_NAME
		);
		return uniqueBugDocumentHandler.getResult();
	}

}
