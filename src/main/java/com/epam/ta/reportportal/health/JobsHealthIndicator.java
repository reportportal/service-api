/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.health;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


/**
 * Health Indicator for jobs service.
 *
 * @author Siarhei Hrabko
 */
@Component
public class JobsHealthIndicator extends AbstractHealthIndicator {

  private static Logger LOGGER = LoggerFactory.getLogger(JobsHealthIndicator.class);
  private static final String ERROR_MESSAGE = "Jobs service health check failed";
  private final RestTemplate restTemplate;

  @Value("${rp.jobs.baseUrl}")
  private String jobsBaseUrl;

  public JobsHealthIndicator(RestTemplate restTemplate) {
    super(ERROR_MESSAGE);
    this.restTemplate = restTemplate;
  }

  @Override
  protected void doHealthCheck(Builder builder) {
    try {
      var jobsHealthRs = restTemplate.getForObject(jobsBaseUrl + "/health", Map.class);

      var jobsStatus = new Status((String) jobsHealthRs.get("status"));
      builder.status(jobsStatus);

      Optional.ofNullable(jobsHealthRs.get("components"))
          .map(Map.class::cast)
          .ifPresent(builder::withDetails);

      builder.build();

    } catch (Exception e) {
      LOGGER.error("{} : {}", ERROR_MESSAGE, e.getMessage());
      builder.unknown()
          .withException(e)
          .build();
    }
  }

  @Override
  public Health getHealth(boolean includeDetails) {
    return super.getHealth(includeDetails);
  }

}
