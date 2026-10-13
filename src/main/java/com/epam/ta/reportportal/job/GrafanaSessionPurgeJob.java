/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.dao.GrafanaSessionRepository;
import java.time.Instant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quartz job that removes expired rows from the {@code public.grafana_session} table.
 *
 * <p>Runs on the cluster-wide Quartz schedule configured via
 * {@code com.ta.reportportal.job.purge.grafana.sessions.cron}, so only one service-api instance executes the purge
 * per fire time.
 *
 * @author Siarhei Hrabko
 */
@Service
public class GrafanaSessionPurgeJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrafanaSessionPurgeJob.class);

  @Autowired
  private GrafanaSessionRepository grafanaSessionRepository;

  @Override
  @Transactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    grafanaSessionRepository.deleteExpired(Instant.now());
    LOGGER.info("Purged expired entries from grafana_session table");
  }
}
