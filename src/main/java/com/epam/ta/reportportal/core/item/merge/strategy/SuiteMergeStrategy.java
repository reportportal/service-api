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

package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SuiteMergeStrategy extends AbstractSuiteMergeStrategy {

	@Autowired
	public SuiteMergeStrategy(TestItemRepository testItemRepository) {
		super(testItemRepository);
	}

	@Override
	public TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items) {
		return moveAllChildTestItems(itemTarget, items);
	}

	@Override
	public boolean isTestItemAcceptableToMerge(TestItem item) {
		if (!item.getType().sameLevel(TestItemType.SUITE)) {
			return false;
		}
		List<TestItem> childItems = testItemRepository.findAllDescendants(item.getId());
		List<TestItem> tests = childItems.stream().filter(child -> !child.getType().sameLevel(TestItemType.SUITE)).collect(toList());
		Set<String> names = tests.stream().map(TestItem::getName).collect(toSet());
		return names.size() == tests.size();
	}
}
