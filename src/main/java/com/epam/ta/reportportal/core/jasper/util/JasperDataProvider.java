/*
 * Copyright 2019 EPAM Systems
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
package com.epam.ta.reportportal.core.jasper.util;

import com.epam.ta.reportportal.core.jasper.TestItemPojo;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Initial {@see JRDataSource} provider class for RP Jasper Reports
 *
 * @author Andrei_Ramanchuk
 */
@Service("jasperDataProvider")
public class JasperDataProvider {

	private TestItemRepository testItemRepository;

	@Autowired
	public JasperDataProvider(TestItemRepository testItemRepository) {
		this.testItemRepository = checkNotNull(testItemRepository);
	}

	public List<TestItemPojo> getTestItemsOfLaunch(Launch launch) {
		/* Get launch referred test items with SORT! */
		return testItemRepository.findTestItemsByLaunchIdOrderByStartTimeAsc(launch.getId())
				.stream()
				.map(TestItemPojo::new)
				.collect(Collectors.toList());
	}
}