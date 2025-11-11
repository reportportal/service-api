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

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.attachment.Attachment;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface AttachmentRepositoryCustom {

  /**
   * Moves attachments to the deletion table for further removing from file storage by project
   *
   * @param projectId Project id
   * @return Number of moved attachments
   */
  int moveForDeletionByProjectId(Long projectId);

  /**
   * Moves attachments to the deletion table for further removing from file storage by launch
   *
   * @param launchId Launch id
   * @return Number of moved attachments
   */
  int moveForDeletionByLaunchId(Long launchId);

  /**
   * Moves attachments to the deletion table for further removing from file storage by launches
   *
   * @param launchIds Launch ids
   * @return Number of moved attachments
   */
  int moveForDeletionByLaunchIds(Collection<Long> launchIds);

  /**
   * Moves attachments to the deletion table for further removing from file storage by items
   *
   * @param itemIds Collection of item ids
   * @return Number of moved attachments
   */
  int moveForDeletionByItems(Collection<Long> itemIds);

  /**
   * Moves attachment to the deletion table for further removing from file storage by id
   *
   * @param attachmentId Attachment id
   * @return Number of moved attachments
   */
  int moveForDeletion(Long attachmentId);

  /**
   * Moves attachments to the deletion table for further removing from file storage by id
   *
   * @param attachmentIds Collection of attachments ids
   * @return Number of moved attachments
   */
  int moveForDeletion(Collection<Long> attachmentIds);

  Page<Long> findIdsByProjectId(Long projectId, Pageable pageable);

  Page<Long> findIdsByLaunchId(Long launchId, Pageable pageable);

  Page<Long> findIdsByTestItemId(Collection<Long> itemIds, Pageable pageable);

  int deleteAllByIds(Collection<Long> ids);

  List<Attachment> findByItemIdsAndLogTimeBefore(Collection<Long> itemIds, Instant before);

  List<Attachment> findByLaunchIdsAndLogTimeBefore(Collection<Long> launchIds,
      Instant before);

  List<Attachment> findByProjectIdsAndLogTimeBefore(Long projectId, Instant before, int limit,
      long offset);
}
