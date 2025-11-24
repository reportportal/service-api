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

package com.epam.reportportal.infrastructure.persistence.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueType;
import java.util.List;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/issue-type/issue-type-fill.sql")
class IssueTypeRepositoryTest extends BaseMvcTest {

  private static final int DEFAULT_ISSUE_TYPES_COUNT = TestItemIssueGroup.values().length - 1;

  @Autowired
  private IssueTypeRepository repository;

  private static void assertIssueType(IssueType customIssueType) {
    final String customLocator = "pb_ajf7d5d";
    final String customLongName = "Custom";
    final String customShortName = "CS";
    final String customHexColor = "#a3847e";

    assertEquals(customLocator, customIssueType.getLocator(), "Incorrect locator");
    assertEquals(customLongName, customIssueType.getLongName(), "Incorrect long name");
    assertEquals(customShortName, customIssueType.getShortName(), "Incorrect short name");
    assertEquals(customHexColor, customIssueType.getHexColor(), "Incorrect hex color");
    assertEquals(TestItemIssueGroup.PRODUCT_BUG,
        customIssueType.getIssueGroup().getTestItemIssueGroup(), "Unexpected issue group");
  }

  @Test
  void findByLocator() {
    final String customLocator = "pb_ajf7d5d";
    final Optional<IssueType> customIssueType = repository.findByLocator(customLocator);
    assertThat("IssueType should exist", customIssueType.isPresent(), Matchers.is(true));
    assertIssueType(customIssueType.get());
  }

  @Test
  void findById() {
    final Long customId = 100L;

    final Optional<IssueType> issueTypeOptional = repository.findById(customId);
    assertTrue(issueTypeOptional.isPresent());
    assertIssueType(issueTypeOptional.get());
  }

  @Test
  void defaultIssueTypes() {
    final List<IssueType> defaultIssueTypes = repository.getDefaultIssueTypes();
    assertEquals(DEFAULT_ISSUE_TYPES_COUNT, defaultIssueTypes.size());
    defaultIssueTypes.forEach(Assertions::assertNotNull);
  }

  @Test
  void getIssueTypeIdsByLocators() {
    final List<Long> types = repository.getIssueTypeIdsByLocators(List.of("pb_ajf7d5d"));
    assertEquals(List.of(100L), types);
  }
}
