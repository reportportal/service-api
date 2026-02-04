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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/fill/item/items-fill.sql")
class AttachmentRepositoryTest extends BaseMvcTest {

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Test
  void updateLaunchIdByProjectIdAndLaunchId() {
    final List<Attachment> firstLaunchAttachments = attachmentRepository.findAllByLaunchIdIn(
        Lists.newArrayList(1L));
    Assertions.assertFalse(firstLaunchAttachments.isEmpty());
    attachmentRepository.updateLaunchIdByProjectIdAndLaunchId(1L, 1L, 2L);

    final List<Attachment> secondLaunchAttachments = attachmentRepository.findAllByLaunchIdIn(
        Lists.newArrayList(2L));

    final List<Long> secondLaunchAttachmentsIds = secondLaunchAttachments.stream()
        .map(Attachment::getId).collect(Collectors.toList());
    Assertions.assertFalse(secondLaunchAttachments.isEmpty());
    Assertions.assertTrue(secondLaunchAttachmentsIds.containsAll(firstLaunchAttachments.stream()
        .map(Attachment::getId)
        .collect(Collectors.toList())));

    Assertions.assertTrue(
        attachmentRepository.findAllByLaunchIdIn(Lists.newArrayList(1L)).isEmpty());
  }

  @Test
  void findAllByProjectId() {

    List<Long> ids = attachmentRepository.findIdsByProjectId(1L, PageRequest.of(0, 50))
        .getContent();

    Assertions.assertFalse(ids.isEmpty());

  }

  @Test
  void findAllByLaunchId() {

    List<Long> ids = attachmentRepository.findIdsByLaunchId(1L, PageRequest.of(0, 50)).getContent();

    Assertions.assertFalse(ids.isEmpty());

  }

  @Test
  void findAllByItemId() {

    List<Long> ids = attachmentRepository.findIdsByTestItemId(Collections.singleton(3L),
        PageRequest.of(0, 50)).getContent();

    Assertions.assertFalse(ids.isEmpty());

  }

  @Test
  void deleteAllByIds() {
    List<Long> ids = attachmentRepository.findAll().stream().limit(4).map(Attachment::getId)
        .collect(Collectors.toList());

    int count = attachmentRepository.deleteAllByIds(ids);

    assertEquals(ids.size(), count);
  }

  @Test
  void findByItemIdsAndPeriodTest() {
    Duration duration = Duration.ofDays(6).plusHours(23);
    final Long itemId = 3L;

    List<Attachment> attachments = attachmentRepository.findByItemIdsAndLogTimeBefore(
        Collections.singletonList(itemId), Instant.now().minus(duration)
    );

    assertTrue(CollectionUtils.isNotEmpty(attachments), "Attachments should not be empty");
    assertEquals(3, attachments.size(), "Incorrect count of attachments");
    attachments.stream().map(it -> null != it.getFileId() || null != it.getThumbnailId())
        .forEach(Assertions::assertTrue);
    attachments.stream().map(Attachment::getFileSize).forEach(size -> assertEquals(1024, size));
  }

  @Test
  void findByLaunchIdsAndPeriodTest() {
    Duration duration = Duration.ofDays(6).plusHours(23);
    final Long launchId = 3L;

    List<Attachment> attachments = attachmentRepository.findByLaunchIdsAndLogTimeBefore(
        Collections.singletonList(launchId),
        Instant.now().minus(duration)
    );

    assertTrue(CollectionUtils.isNotEmpty(attachments), "Attachments should not be empty");
    assertEquals(1, attachments.size(), "Incorrect count of attachments");
    attachments.stream().map(it -> null != it.getFileId() || null != it.getThumbnailId())
        .forEach(Assertions::assertTrue);
    attachments.stream().map(Attachment::getFileSize).forEach(size -> assertEquals(1024, size));
  }

  @Test
  void shouldNotFindByLaunchIdsWhenLessThenPeriod() {
    Duration duration = Duration.ofDays(14).plusHours(23);
    final Long launchId = 3L;

    List<Attachment> attachments = attachmentRepository.findByLaunchIdsAndLogTimeBefore(
        Collections.singletonList(launchId),
        Instant.now().minus(duration)
    );

    assertTrue(CollectionUtils.isEmpty(attachments), "Attachments should be empty");
  }

  @Test
  void findByProjectIdsAndLogTimeBeforeTest() {
    Duration duration = Duration.ofDays(6).plusHours(23);
    final Long projectId = 1L;

    List<Attachment> attachments = attachmentRepository.findByProjectIdsAndLogTimeBefore(projectId,
        Instant.now().minus(duration), 3, 6
    );

    assertTrue(CollectionUtils.isNotEmpty(attachments), "Attachments should not be empty");
    assertEquals(3, attachments.size(), "Incorrect count of attachments");
    attachments.stream().map(it -> null != it.getFileId() || null != it.getThumbnailId())
        .forEach(Assertions::assertTrue);
    attachments.stream().map(Attachment::getFileSize).forEach(size -> assertEquals(1024, size));
  }

  @Test
  void deleteAllByLaunchIdsIn() {
    List<Long> launchIds = Arrays.asList(1L, 2L);
    List<Attachment> attachments = attachmentRepository.findAllByLaunchIdIn(launchIds);
    assertNotNull(attachments);
    assertEquals(9, attachments.size());
    attachments.stream().map(Attachment::getLaunchId).distinct().map(launchIds::contains)
        .forEach(Assertions::assertTrue);
  }
}
