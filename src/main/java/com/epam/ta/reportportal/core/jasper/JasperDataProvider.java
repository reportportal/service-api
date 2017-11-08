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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.jasper;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Initial {@see JRDataSource} provider class for RP Jasper Reports
 *
 * @author Andrei_Ramanchuk
 * <p>
 * TODO Refactor recursive function
 */
@Service("jasperDataProvider")
public class JasperDataProvider {
	private static final String PREFIX = "    ";

	private TestItemRepository testItemRepository;

	@Autowired
	public JasperDataProvider(TestItemRepository testItemRepository) {
		this.testItemRepository = checkNotNull(testItemRepository);
	}

	public List<TestItemPojo> getReportSource(Launch launch) {
		List<TestItemPojo> result = Collections.emptyList();

		/* Get launch referred test items with SORT! */
		List<TestItem> ownedItems = testItemRepository.findByLaunch(launch);
		if (ownedItems.size() > 0) {

			/* Grouping test items by path field */
			Map<List<String>, List<TestItem>> grouped = ownedItems.stream()
					.map(JasperDataProvider::adjustName)
					.collect(Collectors.groupingBy(TestItem::getPath));

			/* List of grouped test items by parent nodes */
			List<TestItem> prepared = this.processLaunchTree(grouped, Lists.newArrayList());

			result = prepared.stream().map(TestItemPojo::new).collect(Collectors.toList());
		}
		return result;
	}

	/**
	 * Recursive method for Map processing and building user friendly beans tree for report.
	 *
	 * @param input
	 * @param processing
	 * @return List<TestItem> - processed list of test items ordered by levels and start time
	 */
	private List<TestItem> processLaunchTree(Map<List<String>, List<TestItem>> input, List<TestItem> processing) {
		Map<List<String>, List<TestItem>> c = new LinkedHashMap<>(input);
		List<String> currentKey = Lists.newArrayList();

		/* Finally empty map */
		if (c.get(Lists.newArrayList()).size() == 0) {
			return processing;
		}

		/* First level ID */
		TestItem zero = c.get(Lists.newArrayList()).get(0);
		currentKey.add(zero.getId());
		if (!processing.contains(zero)) {
			processing.add(zero);
		}

		while ((null != c.get(currentKey)) && (c.get(currentKey).size() > 0)) {
			List<TestItem> value = c.get(currentKey);

			TestItem first = value.get(0);
			if (!first.hasChilds()) {
				value.stream().forEach(v -> {
					if (!processing.contains(v)) {
						processing.add(v);
					}
				});
				c.remove(currentKey);
			} else {
				if (!processing.contains(first)) {
					processing.add(first);
				}
				currentKey.add(first.getId());
			}
		}

		/* Remove processed block reference */
		c.remove(currentKey);

		/* Last processed element is empty after children removing */
		String last = currentKey.get(currentKey.size() - 1);
		currentKey.remove(last);

		/* Remove reference from UP map block to last element */
		c.put(currentKey, c.get(currentKey).stream().filter(f -> !f.getId().equals(last)).collect(Collectors.toList()));
		return processLaunchTree(c, processing);
	}

	/**
	 * Add right shifting for child items depends on depth level
	 *
	 * @param input - target {@see TestItem}
	 * @return TestItem - updated test item with shifted name
	 */
	private static TestItem adjustName(TestItem input) {
		/* Sync buffer instead builder! */
		StringBuilder sb = new StringBuilder(StringUtils.repeat(PREFIX, input.getPath().size()));
		input.setName(sb.append(input.getName()).toString());
		return input;
	}
}