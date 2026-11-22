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

import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import java.time.Duration;
import java.time.Instant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deletes expired self-registration (creation bid) records: invitations ({@code rp.invitation.bid.ttl}) and pending
 * email changes ({@code rp.email.change.ttl}) share the table, so a row is deleted once it is older than the longer of
 * the two lifetimes. Each lifetime is still enforced exactly when its link is used.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class CleanExpiredCreationBidsJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(CleanExpiredCreationBidsJob.class);

  @Autowired
  private UserCreationBidRepository repository;

  @Value("${rp.invitation.bid.ttl:PT24H}")
  private Duration invitationBidTtl = Duration.ofHours(24);

  @Value("${rp.email.change.ttl:PT24H}")
  private Duration emailChangeTtl = Duration.ofHours(24);

  @Override
  @Transactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    int deletedCount = repository.expireBidsOlderThan(Instant.now().minus(retention()));
    LOGGER.info("Cleaning expired user creation bids finished. Deleted {} bids", deletedCount);
  }

  /**
   * @return how long a bid is kept: the longer of the invitation and email-change lifetimes
   */
  Duration retention() {
    return invitationBidTtl.compareTo(emailChangeTtl) >= 0 ? invitationBidTtl : emailChangeTtl;
  }
}
