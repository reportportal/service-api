/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.utils.item.content;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdaterContent;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class TestItemUpdaterContent implements ResourceUpdaterContent {

  private final Long projectId;
  private final List<TestItem> testItems;

  private TestItemUpdaterContent(Long projectId, List<TestItem> testItems) {
    this.projectId = projectId;
    this.testItems = testItems;
  }

  public Long getProjectId() {
    return projectId;
  }

  public List<TestItem> getTestItems() {
    return testItems;
  }

  public static TestItemUpdaterContent of(Long projectId, List<TestItem> testItems) {
    return new TestItemUpdaterContent(projectId, testItems);
  }
}
