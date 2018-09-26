/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author Ivan Budaev
 */
public class DeepMergeStrategy extends AbstractSuiteMergeStrategy {

	@Autowired
	public DeepMergeStrategy(TestItemRepository testItemRepository) {
		super(testItemRepository);
	}

	@Override
	protected void mergeAllChildItems(TestItem testItemParent) {
		testItemRepository.selectAllDescendantsWithChildren(testItemParent.getItemId())
				.stream()
				.collect(groupingBy(TestItem::getUniqueId))
				.entrySet()
				.stream()
				.map(Map.Entry::getValue)
				.filter(items -> items.size() > 1)
				.forEach(items -> mergeTestItems(items.get(0), items.subList(1, items.size())));
	}

	@Override
	public boolean isTestItemAcceptableToMerge(TestItem item) {
		//DeepMerge special condition already implemented in the database query:
		return true;
	}
}
