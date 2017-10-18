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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.Parameter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
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

	private LaunchRepository launchRepository;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Override
	public String generate(TestItem testItem) {
		String forEncoding = prepareForEncoding(testItem);
		return TRAIT + DigestUtils.md5Hex(forEncoding);
	}

	@Override
	public boolean validate(String encoded) {
		return !Strings.isNullOrEmpty(encoded) && encoded.startsWith(TRAIT);
	}

	private String prepareForEncoding(TestItem testItem) {
		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		String launchName = launch.getName();
		String projectName = launch.getProjectRef();
		List<String> pathNames = getPathNames(testItem.getPath());
		String itemName = testItem.getName();
		List<Parameter> parameters = Optional.ofNullable(testItem.getParameters()).orElse(Collections.emptyList());
		StringJoiner joiner = new StringJoiner(";");
		joiner.add(projectName).add(launchName);
		if (!CollectionUtils.isEmpty(pathNames)) {
			joiner.add(pathNames.stream().collect(Collectors.joining(",")));
		}
		joiner.add(itemName);
		if (!parameters.isEmpty()) {
			joiner.add(parameters.stream()
					.map(parameter -> (!Strings.isNullOrEmpty(parameter.getKey()) ? parameter.getKey() + "=" : "") + parameter.getValue())
					.collect(Collectors.joining(",")));
		}
		return joiner.toString();
	}

	private List<String> getPathNames(List<String> path) {
		Map<String, String> names = testItemRepository.findPathNames(path);
		return path.stream().map(names::get).collect(Collectors.toList());
	}
}