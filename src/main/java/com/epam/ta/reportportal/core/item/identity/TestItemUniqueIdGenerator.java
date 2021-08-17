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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
	public String generate(TestItem testItem, List<Long> parentIds, Launch launch, Map<Long, String> testItemNamesCache) {
		String forEncoding = prepareForEncoding(testItem, parentIds, launch, testItemNamesCache);
		return TRAIT + DigestUtils.md5Hex(forEncoding);
	}

	@Override
	public boolean validate(String encoded) {
		return !Strings.isNullOrEmpty(encoded) && encoded.startsWith(TRAIT);
	}

	private String prepareForEncoding(TestItem testItem, List<Long> parentIds, Launch launch, Map<Long, String> testItemNamesCache) {
		Long projectId = launch.getProjectId();
		String launchName = launch.getName();
		List<String> pathNames = getPathNames(parentIds, testItemNamesCache);
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

	private List<String> getPathNames(List<Long> parentIds, Map<Long, String> testItemNamesCache) {
		return parentIds.stream()
				.sorted()
				.map(id -> {
					if (!testItemNamesCache.containsKey(id)) {
						TestItem testItem = testItemRepository.findById(id).orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, id));
						testItemNamesCache.put(id, testItem.getName());
					}
					return testItemNamesCache.get(id);
				})
				.collect(Collectors.toList());
	}
}