/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.utils.item.retriever;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.utils.ResourceContentRetriever;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.converter.utils.item.content.TestItemResourceUpdaterContent;
import com.epam.ta.reportportal.ws.converter.utils.item.updater.PathNameResourceUpdater;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PathNamesRetriever implements ResourceContentRetriever<TestItemResourceUpdaterContent, TestItemResource> {

	private final TestItemRepository testItemRepository;

	@Autowired
	public PathNamesRetriever(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public ResourceUpdater<TestItemResource> retrieve(TestItemResourceUpdaterContent updaterContent) {
		Map<Long, PathName> pathNamesMapping = testItemRepository.selectPathNames(updaterContent.getTestItems()
				.stream()
				.map(TestItem::getItemId)
				.collect(Collectors.toList()), updaterContent.getProjectId());
		return PathNameResourceUpdater.of(pathNamesMapping);
	}
}
