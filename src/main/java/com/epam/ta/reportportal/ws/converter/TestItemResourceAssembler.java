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

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.controller.ITestItemController;
import com.epam.ta.reportportal.ws.controller.impl.TestItemController;
import com.epam.ta.reportportal.ws.converter.builders.TestItemResourceBuilder;
import com.epam.ta.reportportal.ws.model.TestItemResource;

@Service
public class TestItemResourceAssembler extends ProjectRelatedResourceAssembler<TestItem, TestItemResource> {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LaunchRepository launchRepository;

	public TestItemResourceAssembler() {
		super(ITestItemController.class, TestItemResource.class);
	}

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
	public PagedResources<TestItemResource> toPagedResources(Page<TestItem> content, String project) {
		PagedResources<TestItemResource> resources = super.toPagedResources(content, project);
		// load path elements names for all page
		Map<String, String> allPathsNames = getPagePathNames(content);
		// add names to resources
		setPathElementsNames(resources, allPathsNames);
		return resources;
	}

	@Override
	public TestItemResource toResource(TestItem item) {
		TestItemResource testItemResource = this.toResource(item, null);
		testItemResource.setPathNames(getItemName(item.getPath()));
		return testItemResource;
	}

	@Override
	public TestItemResource toResource(TestItem item, String projectName) {
		// initialize map of path names
		Map<String, String> pathNamesInitValue = new LinkedHashMap<>();
		for (String pathElement : item.getPath()) {
			pathNamesInitValue.put(pathElement, null);
		}
		return new TestItemResourceBuilder().addTestItem(item, null).addPathNames(pathNamesInitValue)
				.addLink(ControllerLinkBuilder
						.linkTo(TestItemController.class,
								projectName == null ? launchRepository.findOne(item.getLaunchRef()).getProjectRef() : projectName)
						.slash(item).withSelfRel())
				.build();
	}

	/**
	 * Load path elements names for input items collection
	 *
	 * @param content
	 * @return
	 */
	private Map<String, String> getPagePathNames(Iterable<TestItem> content) {
		Set<String> allPathsIds = new HashSet<>();
		// merge path elements ids for all page element
		for (TestItem testItem : content) {
			allPathsIds.addAll(testItem.getPath());
		}
		// load path names for all page
		List<String> allIds = new ArrayList<>();
		allIds.addAll(allPathsIds);
		return getItemName(allIds);
	}

	/**
	 * Set path elements names for input page using input names map
	 *
	 * @param resources
	 * @param allPathsNames
	 */
	private void setPathElementsNames(PagedResources<TestItemResource> resources, Map<String, String> allPathsNames) {
		for (TestItemResource testItemResource : resources) {
			Set<String> pathIds = testItemResource.getPathNames().keySet();
			for (String pathId : pathIds) {
				testItemResource.getPathNames().put(pathId, allPathsNames.get(pathId));
			}
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