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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrei Varabyeu
 */
@Component
@Transactional
public class ActivityConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityConsumer.class);

  private final ActivityRepository activityRepository;

  @Autowired
  public ActivityConsumer(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  @RabbitListener(queues = "#{ @activityQueue.name }",
      containerFactory = "rabbitListenerContainerFactory")
  public void onEvent(@Payload Activity rq) {
    Optional.ofNullable(rq).ifPresent(this::processActivity);
  }

  private void processActivity(Activity activity) {
    LOGGER.info("[audit] - {}", activity);
    if (activity.isSavedEvent()) {
      if (Objects.isNull(activity.getDetails())) {
        activity.setDetails(new ActivityDetails());
      }
      activityRepository.save(activity);
    }
  }

}
