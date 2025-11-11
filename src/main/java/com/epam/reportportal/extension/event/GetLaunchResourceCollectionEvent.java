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

package com.epam.reportportal.extension.event;

import com.epam.reportportal.reporting.LaunchResource;
import java.util.Collection;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
// TODO: replace with different dto object to unwire commons-reporting lib
public class GetLaunchResourceCollectionEvent extends EntityCollectionEvent<LaunchResource> {

  public GetLaunchResourceCollectionEvent(Collection<LaunchResource> entities) {
    super(entities);
  }
}
