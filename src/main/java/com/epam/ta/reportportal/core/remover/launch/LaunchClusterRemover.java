/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.remover.launch;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchClusterRemover implements ContentRemover<Launch> {

  private final ClusterRepository clusterRepository;

  @Autowired
  public LaunchClusterRemover(ClusterRepository clusterRepository) {
    this.clusterRepository = clusterRepository;
  }

  @Override
  public void remove(Launch launch) {
    clusterRepository.deleteClusterTestItemsByLaunchId(launch.getId());
    clusterRepository.deleteAllByLaunchId(launch.getId());
  }
}
