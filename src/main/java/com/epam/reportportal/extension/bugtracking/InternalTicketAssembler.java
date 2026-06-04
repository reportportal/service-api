/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.extension.bugtracking;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.model.externalsystem.PostFormField;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.base.infrastructure.persistence.binary.impl.AttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataEncoder;
import com.epam.reportportal.extension.util.FileNameExtractor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Converts REST model into internal domain model representation
 *
 * @author Andrei Varabyeu
 */
@Service
public class InternalTicketAssembler implements Function<PostTicketRQ, InternalTicket> {

  private final LogRepository logRepository;

  private final TestItemRepository itemRepository;

  private final AttachmentDataStoreService attachmentDataStoreService;

  private final DataEncoder dataEncoder;

  @Autowired
  public InternalTicketAssembler(LogRepository logRepository, TestItemRepository itemRepository,
      AttachmentDataStoreService attachmentDataStoreService, DataEncoder dataEncoder) {
    this.logRepository = logRepository;
    this.itemRepository = itemRepository;
    this.attachmentDataStoreService = attachmentDataStoreService;
    this.dataEncoder = dataEncoder;
  }

  @Override
  public InternalTicket apply(PostTicketRQ input) {
    InternalTicket ticket = new InternalTicket();

    ofNullable(input.getFields()).ifPresent(fields -> ticket.setFields(getFieldsMap(fields)));

    if (input.getIsIncludeLogs() || input.getIsIncludeScreenshots()) {
      ticket.setLogs(getLogEntries(input));
    }

    if (input.getIsIncludeComments()) {
      itemRepository.findById(input.getTestItemId())
          .ifPresent(item -> ofNullable(item.getItemResults()
              .getIssue()).ifPresent(issue -> ofNullable(issue.getIssueDescription()).ifPresent(ticket::setComments)));

    }

    if (!CommonPredicates.IS_MAP_EMPTY.test(input.getBackLinks())) {
      ticket.setBackLinks(ImmutableMap.copyOf(input.getBackLinks()));
    }
    return ticket;
  }

  private Multimap<String, String> getFieldsMap(List<PostFormField> postFormFields) {
    Multimap<String, String> fieldsMap = LinkedListMultimap.create(postFormFields.size());
    postFormFields.forEach(
        f -> fieldsMap.putAll(f.getId(), ofNullable(f.getValue()).orElseGet(Collections::emptyList)));
    return fieldsMap;
  }

  private List<InternalTicket.LogEntry> getLogEntries(PostTicketRQ input) {
    List<Log> logs = logRepository.findByTestItemId(input.getTestItemId(),
        0 == input.getNumberOfLogs() ? Integer.MAX_VALUE : input.getNumberOfLogs()
    );

    return logs.stream().map(l -> {
      /* Get screenshots if required and they are present */
      if (null != l.getAttachment() && input.getIsIncludeScreenshots()) {
        return new InternalTicket.LogEntry(l.getId(),
            l.getLogMessage(),
            input.getIsIncludeLogs(),
            true,
            l.getAttachment().getFileId(),
            FileNameExtractor.extractFileName(dataEncoder, l.getAttachment().getFileId()),
            l.getAttachment().getContentType()
        );
      }
      /* Forwarding enabled logs boolean if screens only required */
      return new InternalTicket.LogEntry(l.getId(), l.getLogMessage(), input.getIsIncludeLogs());
    }).collect(Collectors.toList());
  }
}
