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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
public class TestItemResourceAssembler extends PagedResourcesAssembler<TestItem, TestItemResource> {

	@Autowired
	private TestItemRepository testItemRepository;

	/*
	 * (non-Javadoc) Modified resource building mechanism(added simple resource
	 * post construction) for fixing performance issue. Path elements names is
	 * loading only one time for all page items at once and loaded names are
	 * using for updating constructed resources.
	 *
	 * @see
	 * com.epam.ta.reportportal.ws.converter.ProjectRelatedResourceAssembler#
	 * toPagedResources(org.springframework.data.domain.Page, java.lang.String)
	 */
	@Override
	public com.epam.ta.reportportal.ws.model.Page<TestItemResource> toPagedResources(Page<TestItem> content) {
		com.epam.ta.reportportal.ws.model.Page<TestItemResource> resources = super.toPagedResources(content);
		// load path elements names for all page
		Map<String, String> allPathsNames = getPagePathNames(content);
		// add names to resources
		setPathElementsNames(resources, allPathsNames);
		return resources;
	}

	@Override
	public TestItemResource toResource(TestItem entity) {
		TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(entity);
		resource.setPathNames(getItemName(entity.getPath()));
		return resource;
	}

	//TODO check path names
	public TestItemResource toResource(TestItem item, String launchStatus) {
		TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(item);
		resource.setLaunchStatus(launchStatus);
		resource.setPathNames(getItemName(item.getPath()));
		return resource;
	}

	/**
	 * Load path elements names for input items collection
	 *
	 * @param content
	 * @return
	 */
	private Map<String, String> getPagePathNames(Iterable<TestItem> content) {
		return getItemName(
				StreamSupport.stream(content.spliterator(), false).flatMap(it -> it.getPath().stream()).distinct().collect(toList()));
	}

	/**
	 * Set path elements names for input page using input names map
	 *
	 * @param resources
	 * @param allPathsNames
	 */
	private void setPathElementsNames(com.epam.ta.reportportal.ws.model.Page<TestItemResource> resources,
			Map<String, String> allPathsNames) {
		for (TestItemResource testItemResource : resources) {
			testItemResource.getPathNames().keySet().forEach(pathId -> {
				testItemResource.getPathNames().put(pathId, allPathsNames.get(pathId));
			});
		}
	}

	/**
	 * Get names of test items via their IDs
	 *
	 * @param ids
	 * @return
	 */
	private Map<String, String> getItemName(List<String> ids) {
		Map<String, String> result = testItemRepository.findPathNames(ids);
		LinkedHashMap<String, String> orderedResult = new LinkedHashMap<>();
		for (String id : ids) {
			orderedResult.put(id, result.get(id));
		}
		return orderedResult;
	}
}
