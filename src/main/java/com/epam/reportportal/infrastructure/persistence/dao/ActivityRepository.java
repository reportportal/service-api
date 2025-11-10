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

import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for {@link Activity} entity
 *
 * @author Andrei Varabyeu
 */
public interface ActivityRepository extends ReportPortalRepository<Activity, Long>,
    ActivityRepositoryCustom {

  @Query(value = "SELECT DISTINCT a.subject_name FROM Activity a WHERE a.project_id = :projectId AND LOWER(a.subject_name) LIKE %:value%", nativeQuery = true)
  List<String> findSubjectNameByProjectIdAndSubjectName(Long projectId, String value);

}
