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
import com.epam.ta.reportportal.database.entity.item.Parameter;
import com.epam.ta.reportportal.database.entity.item.TestItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public abstract class AbstractSuiteMergeStrategy implements MergeStrategy {

	protected final TestItemRepository testItemRepository;

	AbstractSuiteMergeStrategy(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public abstract TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items);

	public abstract boolean isTestItemAcceptableToMerge(TestItem item);

	private TestItem moveAllChildTestItems(TestItem itemTarget, TestItem itemSource) {
		for (TestItem childItem : testItemRepository.findAllDescendants(itemSource.getId())) {
			childItem.setParent(itemTarget.getId());
			childItem.setLaunchRef(itemTarget.getLaunchRef());

			List<String> path = new ArrayList<>(itemTarget.getPath());
			path.add(itemTarget.getId());
			childItem.setPath(path);

			setLaunchRefForChildren(childItem, itemTarget.getLaunchRef());
			testItemRepository.save(childItem);
		}
		updateTargetItemInfo(itemTarget, itemSource);
		testItemRepository.delete(itemSource);
		return itemTarget;
	}

	TestItem moveAllChildTestItems(TestItem itemTarget, List<TestItem> items) {
		TestItem result = items.stream().reduce(itemTarget, this::moveAllChildTestItems);
		mergeAllChildItems(result);
		return result;
	}

	private void setLaunchRefForChildren(TestItem testItemParent, String launchRef) {
		List<TestItem> childItems = testItemRepository.findAllDescendants(testItemParent.getId());
		for (TestItem child : childItems) {
			child.setLaunchRef(launchRef);
			List<String> path = new ArrayList<>(testItemParent.getPath());
			path.add(testItemParent.getId());
			child.setPath(path);
			testItemRepository.save(child);
			if (child.hasChilds()) {
				setLaunchRefForChildren(child, launchRef);
			}
		}
	}

	protected void mergeAllChildItems(TestItem testItemParent) {
		List<TestItem> childItems = testItemRepository.findAllDescendants(testItemParent.getId());
		List<TestItem> suites = childItems.stream().filter(this::isTestItemAcceptableToMerge).collect(toList());

		suites.stream()
				.collect(Collectors.groupingBy(TestItem::getName))
				.forEach((key, value) -> moveAllChildTestItems(value.get(0), value.subList(1, value.size())));
	}

	/**
	 * Collects tags, parameters and descriptions from source and add them to target. Same tags
	 * are added only once. Updates start and end times of target. Updates item identifier.
	 *
	 * @param target item to be merged
	 * @param source item to merge
	 */
	private void updateTargetItemInfo(TestItem target, TestItem source) {
		target = updateTime(target, source);
		Set<String> tags = mergeTags(target.getTags(), source.getTags());
		if (!tags.isEmpty()) {
			target.setTags(tags);
		}
		String result = mergeDescriptions(target.getItemDescription(), source.getItemDescription());
		if (!result.isEmpty()) {
			target.setItemDescription(result);
		}
		List<Parameter> parameters = mergeParameters(target.getParameters(), source.getParameters());

		//since merge based on unique id
		if (parameters.equals(source.getParameters())) {
			target.setUniqueId(source.getUniqueId());
		}
		testItemRepository.save(target);
	}

	/**
	 * Defines start time as the earliest and the end time as latest
	 */
	private TestItem updateTime(TestItem target, TestItem source) {
		target.setStartTime(target.getStartTime().before(source.getStartTime()) ? target.getStartTime() : source.getStartTime());
		target.setEndTime(target.getEndTime().after(source.getEndTime()) ? target.getEndTime() : source.getEndTime());
		return target;
	}

	private Set<String> mergeTags(@Nullable Set<String> first, @Nullable Set<String> second) {
		return Stream.concat(first != null ? first.stream() : Stream.empty(), second != null ? second.stream() : Stream.empty())
				.collect(Collectors.toSet());
	}

	private String mergeDescriptions(@Nullable String first, @Nullable String second) {
		return new StringJoiner("\r\n").add(first != null ? first : "").add(second != null ? second : "").toString();
	}

	private List<Parameter> mergeParameters(@Nullable List<Parameter> first, @Nullable List<Parameter> second) {
		return Stream.concat(first != null ? first.stream() : Stream.empty(), second != null ? second.stream() : Stream.empty())
				.collect(Collectors.toList());
	}
}
