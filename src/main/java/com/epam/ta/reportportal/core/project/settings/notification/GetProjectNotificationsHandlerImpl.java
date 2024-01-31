/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.project.settings.notification;

import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.converter.converters.NotificationConfigConverter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class GetProjectNotificationsHandlerImpl implements GetProjectNotificationsHandler {

  private final SenderCaseRepository senderCaseRepository;

  @Autowired
  public GetProjectNotificationsHandlerImpl(SenderCaseRepository senderCaseRepository) {
    this.senderCaseRepository = senderCaseRepository;
  }

  @Override
  public List<SenderCaseDTO> getProjectNotifications(Long projectId) {
    return senderCaseRepository.findAllByProjectId(projectId).stream()
        .map(NotificationConfigConverter.TO_CASE_RESOURCE).collect(Collectors.toList());
  }
}
