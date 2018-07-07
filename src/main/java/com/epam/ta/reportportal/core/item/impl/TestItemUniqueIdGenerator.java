/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.core.item.UniqueIdGenerator;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
	public String generate(TestItem testItem, Launch launch) {
		String forEncoding = prepareForEncoding(testItem, launch);
		return TRAIT + DigestUtils.md5Hex(forEncoding);
	}

	@Override
	public boolean validate(String encoded) {
		return !Strings.isNullOrEmpty(encoded) && encoded.startsWith(TRAIT);
	}

	private String prepareForEncoding(TestItem testItem, Launch launch) {
		Long projectId = launch.getProjectId();
		String launchName = launch.getName();
		List<String> pathNames = new ArrayList<>();
		Optional.ofNullable(testItem.getItemStructure().getParent())
				.ifPresent(it -> pathNames.addAll(
						testItemRepository.selectPathNames(it.getItemId()).entrySet().stream().map(Map.Entry::getValue).collect(toList())));
		String itemName = testItem.getName();
		StringJoiner joiner = new StringJoiner(";");
		joiner.add(projectId.toString()).add(launchName);
		if (!CollectionUtils.isEmpty(pathNames)) {
			joiner.add(pathNames.stream().collect(Collectors.joining(",")));
		}
		joiner.add(itemName);
		List<Parameter> parameters = testItem.getParameters();
		if (!CollectionUtils.isEmpty(parameters)) {
			joiner.add(parameters.stream()
					.map(parameter -> (!Strings.isNullOrEmpty(parameter.getKey()) ? parameter.getKey() + "=" : "") + parameter.getValue())
					.collect(Collectors.joining(",")));
		}
		return joiner.toString();
	}
}