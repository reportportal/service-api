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

import com.epam.ta.reportportal.core.item.merge.MergeStrategy;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemTag;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
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

		Set<TestItemTag> sourceTags = ofNullable(source.getTags()).orElseGet(Sets::newHashSet);
		Set<TestItemTag> targetTags = ofNullable(target.getTags()).orElseGet(Sets::newHashSet);

		sourceTags.removeAll(targetTags);
		targetTags.addAll(sourceTags);

		target.setTags(targetTags);

		String result = mergeDescriptions(target.getDescription(), source.getDescription());
		if (!result.isEmpty()) {
			target.setDescription(result);
		}

		List<Parameter> sourceParameters = ofNullable(source.getParameters()).orElseGet(Lists::newArrayList);
		List<Parameter> targetParameters = ofNullable(target.getParameters()).orElseGet(Lists::newArrayList);

		sourceParameters.removeAll(targetParameters);
		targetParameters.addAll(sourceParameters);

		target.setParameters(targetParameters);

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
