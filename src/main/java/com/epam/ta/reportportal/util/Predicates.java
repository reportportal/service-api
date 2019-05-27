/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.google.common.base.CharMatcher;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Set of common predicates
 *
 * @author Andrei Varabyeu
 */
public class Predicates {

    private static final String SPECIAL_CHARACTERS = "-/@#$%^&_+=()";

    private Predicates() {
        //statics only
    }

    /**
     * Checker whether string contains special characters only
     */
    public static final Predicate<String> SPECIAL_CHARS_ONLY = str -> CharMatcher.anyOf(SPECIAL_CHARACTERS).matchesAllOf(str);

    /**
     * Checks if the test item is suitable for indexing in analyzer.
     */
    public static final Predicate<TestItem> ITEM_CAN_BE_INDEXED = testItem -> testItem != null
            && testItem.getItemResults().getIssue() != null && !TestItemIssueGroup.TO_INVESTIGATE.equals(testItem.getItemResults()
            .getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()) && !testItem.getItemResults()
            .getIssue()
            .getIgnoreAnalyzer();

    /**
     * Checks if the launch is suitable for indexing in analyzer
     */
    public static final Predicate<Launch> LAUNCH_CAN_BE_INDEXED = launch -> launch != null
            && LaunchModeEnum.DEFAULT.equals(launch.getMode());

    /**
     * Checks if not system item attribute has specified key and value
     */
    public static final BiPredicate<ItemAttribute, ItemAttributeResource> ITEM_ATTRIBUTE_EQUIVALENCE = (attribute, resource) -> {
        boolean valueAndSystemEquivalence = attribute.getValue().equals(resource.getValue()) && !attribute.isSystem();
        return Objects.isNull(attribute.getKey()) ? Objects.isNull(resource.getKey()) && valueAndSystemEquivalence : attribute.getKey().equals(resource.getKey()) && valueAndSystemEquivalence;
    };
}
