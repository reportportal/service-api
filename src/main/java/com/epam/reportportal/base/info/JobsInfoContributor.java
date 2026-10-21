/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.info;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


/**
 * Picks actuator info from <i>jobs</i> service and shows it as part of <i>service-api</i>.
 *
 * @author Siarhei Hrabko
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobsInfoContributor implements InfoContributor {

  private final RestClient restClient;

  @Value("${rp.jobs.baseUrl}")
  private String jobsBaseUrl;

  @Override
  public void contribute(Builder builder) {
    try {
      var jobsInfoRs = restClient.get()
          .uri(jobsBaseUrl + "/info")
          .retrieve()
          .body(Map.class);
      builder
          .withDetail("jobsInfo", jobsInfoRs)
          .build();
    } catch (Exception e) {
      log.error("Jobs service was not found");
    }
  }
}
