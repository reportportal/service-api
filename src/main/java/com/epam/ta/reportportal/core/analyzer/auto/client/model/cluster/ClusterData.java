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

package com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ClusterData {

  private Long project;
  private Long launchId;
  private List<ClusterInfoRs> clusters;

  public ClusterData() {
  }

  public Long getProject() {
    return project;
  }

  public void setProject(Long project) {
    this.project = project;
  }

  public Long getLaunchId() {
    return launchId;
  }

  public void setLaunchId(Long launchId) {
    this.launchId = launchId;
  }

  public List<ClusterInfoRs> getClusters() {
    return clusters;
  }

  public void setClusters(List<ClusterInfoRs> clusters) {
    this.clusters = clusters;
  }
}
