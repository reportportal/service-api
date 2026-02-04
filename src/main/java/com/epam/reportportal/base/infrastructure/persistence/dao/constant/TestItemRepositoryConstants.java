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

package com.epam.reportportal.base.infrastructure.persistence.dao.constant;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class TestItemRepositoryConstants {

  public static final String LAUNCH_ID = "launch_id";
  public static final String ITEM_ID = "item_id";
  public static final String HAS_CHILDREN = "has_children";
  public static final String RETRIES_TABLE = "retries";
  public static final String HAS_CONTENT = "hasContent";
  public static final String ATTACHMENTS_COUNT = "attachmentsCount";
  public static final String NESTED = "nested";

  private TestItemRepositoryConstants() {
    //static only
  }
}
