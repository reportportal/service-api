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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.store.database.dao.TestItemStructureRepository;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.service.StatisticsService;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Dzianis_Shybeka
 */
@Service
public class TestItemResourceAssembler extends PagedResourcesAssembler<TestItem, TestItemResource> {

	private final TestItemStructureRepository testItemStructureRepository;
	private final StatisticsService statisticsService;

	public TestItemResourceAssembler(TestItemStructureRepository testItemStructureRepository, StatisticsService statisticsService) {
		this.testItemStructureRepository = testItemStructureRepository;
		this.statisticsService = statisticsService;
	}

	@Override
	public TestItemResource toResource(TestItem entity) {

		TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(entity);

		resource.setHasChilds(testItemStructureRepository.findTopItemIdByParentId(entity.getItemId()).isPresent());

		resource.setRetries(getRetries(entity));

		resource.setPathNames(getPath(entity));

		resource.setStatistics(statisticsService.getStatistics(entity));

		return resource;
	}

	private List<TestItemResource> getRetries(TestItem entity) {
		return testItemStructureRepository.findRetriesOf(entity.getItemId())
				.stream()
				.map(TestItemConverter.TO_RESOURCE)
				.collect(Collectors.toList());
	}

	private Map<String, String> getPath(TestItem entity) {
		return testItemStructureRepository.findPath(entity.getItemId()).stream().collect(Collectors.toMap(
				holder -> Objects.toString(holder.getId(), StringUtils.EMPTY),
				holder -> Objects.toString(holder.getName(), StringUtils.EMPTY)
		));
	}
}
