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

package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AMQP wire DTO for activity events. Decoupled from the JPA {@code Activity} entity so that
 * database-schema changes do not silently alter the message format.
 */
@Getter
@Setter
@NoArgsConstructor
public class ActivityMessage {

  private Instant createdAt;
  private EventAction action;
  private String eventName;
  private EventPriority priority;
  private Long objectId;
  private String objectName;
  private EventObject objectType;
  private Long projectId;
  private ActivityDetails details;
  private Long subjectId;
  private String subjectName;
  private EventSubject subjectType;
  private boolean savedEvent;

}
