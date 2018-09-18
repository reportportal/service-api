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

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.core.item.merge.MergeStrategy;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Ivan Budaev
 */
public abstract class AbstractSuiteMergeStrategy implements MergeStrategy {

	protected final TestItemRepository testItemRepository;

	AbstractSuiteMergeStrategy(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	public abstract boolean isTestItemAcceptableToMerge(TestItem item);

	@Override
	public TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items) {
		TestItem result = items.stream().reduce(itemTarget, this::moveAllChildTestItems);
		TestItem result1 = items.stream().reduce(itemTarget, (prev, curr) -> moveAllChildTestItems(prev, curr));
		this.mergeAllChildItems(result);
		return result;
	}

	protected void mergeAllChildItems(TestItem testItemParent) {
		List<TestItem> childItems = testItemRepository.selectAllDescendants(testItemParent.getItemId());
		List<TestItem> suites = childItems.stream().filter(this::isTestItemAcceptableToMerge).collect(toList());

		suites.stream()
				.collect(Collectors.groupingBy(TestItem::getName))
				.forEach((key, value) -> mergeTestItems(value.get(0), value.subList(1, value.size())));
	}

	private TestItem moveAllChildTestItems(TestItem itemTarget, TestItem itemSource) {
		for (TestItem childItem : testItemRepository.selectAllDescendants(itemSource.getItemId())) {
			childItem.setLaunch(itemTarget.getLaunch());
			childItem.setParent(itemTarget);

			setLaunchRefForChildren(childItem, itemTarget.getLaunch());
			testItemRepository.save(childItem);
		}
		updateTargetItemInfo(itemTarget, itemSource);
		testItemRepository.delete(itemSource);
		return itemTarget;
	}

	private void setLaunchRefForChildren(TestItem testItemParent, Launch launch) {
		List<TestItem> childItems = testItemRepository.selectAllDescendants(testItemParent.getItemId());
		for (TestItem child : childItems) {
			child.setLaunch(launch);
			testItemRepository.save(child);
			setLaunchRefForChildren(child, launch);
		}
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
		source.getTags().removeAll(target.getTags());
		target.getTags().addAll(source.getTags());
		String result = mergeDescriptions(target.getDescription(), source.getDescription());
		if (!result.isEmpty()) {
			target.setDescription(result);
		}
		source.getParameters().removeAll(target.getParameters());
		target.getParameters().addAll(source.getParameters());

		//since merge based on unique id
		//there are no difference for deep merging since only items with the same uniqueId merged
		//TODO: implement some strategy for merging uniqueId in case of other merging strategies
		//		if (target.getParameters().equals(source.getParameters())) {
		//			target.setUniqueId(source.getUniqueId());
		//		}
		testItemRepository.save(target);
	}

	/**
	 * Defines start time as the earliest and the end time as latest
	 */
	private TestItem updateTime(TestItem target, TestItem source) {
		target.setStartTime(target.getStartTime().isBefore(source.getStartTime()) ? target.getStartTime() : source.getStartTime());
		LocalDateTime targetEndTime = target.getItemResults().getEndTime();
		LocalDateTime sourceEndTime = source.getItemResults().getEndTime();
		target.getItemResults().setEndTime(targetEndTime.isAfter(sourceEndTime) ? targetEndTime : sourceEndTime);
		target.setLastModified(LocalDateTime.now());
		return target;
	}

	private String mergeDescriptions(@Nullable String first, @Nullable String second) {
		return new StringJoiner("\r\n").add(first != null ? first : "").add(second != null ? second : "").toString();
	}
}
