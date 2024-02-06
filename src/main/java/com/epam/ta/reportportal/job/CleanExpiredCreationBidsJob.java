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

package com.epam.ta.reportportal.job;

import static com.epam.ta.reportportal.commons.EntityUtils.FROM_UTC_TO_LOCAL_DATE_TIME;

import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import java.time.LocalDateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class CleanExpiredCreationBidsJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(CleanExpiredCreationBidsJob.class);

  @Autowired
  private UserCreationBidRepository repository;

  @Override
  @Transactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    int deletedCount = repository.expireBidsOlderThan(
        FROM_UTC_TO_LOCAL_DATE_TIME.apply(LocalDateTime.now().minusDays(1)));
    LOGGER.info("Cleaning expired user creation bids finished. Deleted {} bids", deletedCount);
  }
}
