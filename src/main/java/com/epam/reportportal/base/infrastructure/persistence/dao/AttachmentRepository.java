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

import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface AttachmentRepository extends ReportPortalRepository<Attachment, Long>,
    AttachmentRepositoryCustom {

  Optional<Attachment> findByFileId(String fileId);

  List<Attachment> findAllByProjectId(Long projectId);

  List<Attachment> findAllByLaunchIdIn(Collection<Long> launchIds);

  void deleteAllByProjectId(Long projectId);

  @Modifying
  @Query(value = "UPDATE attachment SET launch_id = :newLaunchId WHERE project_id = :projectId AND launch_id = :currentLaunchId", nativeQuery = true)
  void updateLaunchIdByProjectIdAndLaunchId(@Param("projectId") Long projectId,
      @Param("currentLaunchId") Long currentLaunchId,
      @Param("newLaunchId") Long newLaunchId);
}
