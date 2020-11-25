/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.identity;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Generates the unique identifier for test item based
 * on Base64 encoding and includes information about project,
 * name of item's launch, full path of item's parent names,
 * item name and parameters.
 *
 * @author Pavel_Bortnik
 */
@Service
public class TestItemUniqueIdGenerator implements UniqueIdGenerator {

	private static final String TRAIT = "auto:";

	private TestItemRepository testItemRepository;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public String generate(TestItem testItem, List<Long> parentIds, Launch launch) {
		String forEncoding = prepareForEncoding(testItem, parentIds, launch);
		return TRAIT + DigestUtils.md5Hex(forEncoding);
	}

	@Override
	public boolean validate(String encoded) {
		return !Strings.isNullOrEmpty(encoded) && encoded.startsWith(TRAIT);
	}

	private String prepareForEncoding(TestItem testItem, List<Long> parentIds, Launch launch) {
		Long projectId = launch.getProjectId();
		String launchName = launch.getName();
		List<String> pathNames = getPathNames(parentIds);
		String itemName = testItem.getName();
		StringJoiner joiner = new StringJoiner(";");
		joiner.add(projectId.toString()).add(launchName);
		if (!CollectionUtils.isEmpty(pathNames)) {
			joiner.add(String.join(";", pathNames));
		}
		joiner.add(itemName);
		Set<Parameter> parameters = testItem.getParameters();
		if (!CollectionUtils.isEmpty(parameters)) {
			joiner.add(parameters.stream()
					.map(parameter -> (!Strings.isNullOrEmpty(parameter.getKey()) ? parameter.getKey() + "=" : "") + parameter.getValue())
					.collect(Collectors.joining(",")));
		}
		return joiner.toString();
	}

	private List<String> getPathNames(List<Long> parentIds) {
		return testItemRepository.findAllById(parentIds)
				.stream()
				.sorted(Comparator.comparingLong(TestItem::getItemId))
				.map(TestItem::getName)
				.collect(Collectors.toList());
	}
}