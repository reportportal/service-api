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

package com.epam.reportportal.base.job;

import com.epam.reportportal.base.infrastructure.persistence.dao.RevokedTokenRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quartz job that removes expired entries from the JWT blacklist ({@code public.revoked_token} table).
 *
 * <p>Runs on the cluster-wide Quartz schedule configured in
 * {@code com.ta.reportportal.job.purge.revoked.tokens.cron}, so only one service-api instance executes the purge per
 * fire time.
 */
@Service
@Slf4j
public class RevokedTokensPurgeJob implements Job {

  @Autowired
  private RevokedTokenRepository revokedTokenRepository;

  @Value("${rp.jwt.token.validity-period}")
  private long accessTokenValiditySeconds;

  /**
   * Deletes every blacklist row whose {@code revoked_at} is older than the maximum JWT lifetime.
   */
  @Override
  @Transactional
  public void execute(JobExecutionContext context) {
    var cutoff = Instant.now().minus(Duration.ofSeconds(accessTokenValiditySeconds));
    revokedTokenRepository.deleteExpired(cutoff);
    log.info("Purged expired entries from revoked_token table");
  }
}
