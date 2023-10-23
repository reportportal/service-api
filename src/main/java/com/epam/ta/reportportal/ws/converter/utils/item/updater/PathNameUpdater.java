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

package com.epam.ta.reportportal.ws.converter.utils.item.updater;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PathNameUpdater implements ResourceUpdater<TestItemResource> {

  private final Map<Long, PathName> pathNamesMapping;

  private PathNameUpdater(Map<Long, PathName> pathNamesMapping) {
    this.pathNamesMapping = pathNamesMapping;
  }

  @Override
  public void updateResource(TestItemResource resource) {
    ofNullable(pathNamesMapping.get(resource.getItemId())).ifPresent(
        pathName -> resource.setPathNames(TestItemConverter.PATH_NAME_TO_RESOURCE
            .apply(pathName)));
  }

  public static PathNameUpdater of(Map<Long, PathName> pathNameMapping) {
    return new PathNameUpdater(pathNameMapping);
  }
}
