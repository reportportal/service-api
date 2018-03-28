/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.strategy;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public class AutoAnalyzedStrategy implements AnalyzeItemsStrategy {

	private TestItemRepository testItemRepository;

	private ILogIndexer logIndexer;

	public AutoAnalyzedStrategy(TestItemRepository testItemRepository, ILogIndexer logIndexer) {
		this.testItemRepository = testItemRepository;
		this.logIndexer = logIndexer;
	}

	@Override
	public List<TestItem> getItems(String project, String launchId) {
		List<TestItem> itemsByAutoAnalyzedStatus = testItemRepository.findItemsByAutoAnalyzedStatus(true, launchId);
		logIndexer.cleanIndex(project, itemsByAutoAnalyzedStatus.stream().map(TestItem::getId).collect(Collectors.toList()));
		return itemsByAutoAnalyzedStatus;
	}
}
