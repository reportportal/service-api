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
import com.epam.ta.reportportal.database.LaunchesDurationDocumentHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Content loader implementation for Launch duration chart widget
 *
 * @author Dzmitry_Kavalets
 */
@Service("LaunchesDurationContentLoader")
public class LaunchesDurationContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {
		if (filter.getTarget().equals(TestItem.class)) {
			return Collections.emptyMap();
		}
		String collectionName = getCollectionName(filter.getTarget());
		List<String> chartFields = ImmutableList.<String>builder().addAll(contentFields).addAll(metaDataFields).build();
		LaunchesDurationDocumentHandler documentHandler = new LaunchesDurationDocumentHandler();
		if (options.containsKey(LATEST_MODE)) {
			launchRepository.findLatestWithCallback(filter, sorting, contentFields, quantity, documentHandler);
		} else {
			launchRepository.loadWithCallback(filter, sorting, quantity, chartFields, documentHandler, collectionName);
		}
		List<ChartObject> result = documentHandler.getResult();
		if (WidgetUtils.needRevert(sorting)) {
			Collections.reverse(result);
		}
		return Collections.singletonMap(RESULT, result);
	}
}