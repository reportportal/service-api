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
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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

    private static final Base64.Encoder encoder = Base64.getEncoder();

    @Autowired
    private TestItemRepository testItemRepository;

    @Autowired
    private LaunchRepository launchRepository;

    public String generate(TestItem testItem, String projectName) {
        String forEncoding = prepareForEncoding(testItem, projectName);
        return encoder.encodeToString(forEncoding.getBytes(StandardCharsets.UTF_8));
    }

    private String prepareForEncoding(TestItem testItem, String projectName) {
        String launchName = launchRepository.findNameNumberAndModeById(testItem.getLaunchRef()).getName();
        List<String> pathNames = getPathNames(testItem.getPath());
        String itemName = testItem.getName();
        List<String> parameters = Optional.ofNullable(testItem.getParameters()).orElse(Collections.emptyList());
        return new StringJoiner("; ").add(projectName).add(launchName).add(pathNames.stream().collect(Collectors.joining())).add(itemName)
                .add(parameters.stream().collect(Collectors.joining())).toString();
    }

    private List<String> getPathNames(List<String> path) {
        Map<String, String> names = testItemRepository.findPathNames(path);
        return path.stream().map(names::get).collect(Collectors.toList());
    }
}
