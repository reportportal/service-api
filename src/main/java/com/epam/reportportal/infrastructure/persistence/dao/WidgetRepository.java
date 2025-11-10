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

import com.epam.reportportal.infrastructure.persistence.entity.widget.Widget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Pavel Bortnik
 */
public interface WidgetRepository extends ReportPortalRepository<Widget, Long>,
    WidgetRepositoryCustom {

  /**
   * Finds widget by 'id' and 'project id'
   *
   * @param id        {@link Widget#id}
   * @param projectId Id of the {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} whose
   *                  widget will be extracted
   * @return {@link Widget} wrapped in the {@link Optional}
   */
  Optional<Widget> findByIdAndProjectId(Long id, Long projectId);

  /**
   * @param projectId Id of the {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} whose
   *                  widgets will be extracted
   * @return The {@link List} of the {@link Widget}
   */
  List<Widget> findAllByProjectId(Long projectId);

  /**
   * Checks the existence of the {@link Widget} with specified name for a user on a project
   *
   * @param name      {@link Widget#name}
   * @param owner     {@link Widget#owner}
   * @param projectId Id of the {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} on which
   *                  widget existence will be checked
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndOwnerAndProjectId(String name, String owner, Long projectId);

  @Query(value = "SELECT w FROM Widget w WHERE w.project.id = :projectId AND w.widgetType IN :widgetTypes")
  List<Widget> findAllByProjectIdAndWidgetTypeIn(@Param("projectId") Long projectId,
      @Param("widgetTypes") List<String> widgetTypes);

  @Query(value = "SELECT w FROM Widget w WHERE w.owner = :owner AND w.widgetType IN :widgetTypes")
  List<Widget> findAllByOwnerAndWidgetTypeIn(@Param("owner") String username,
      @Param("widgetTypes") List<String> widgetTypes);

  @Query(value = "SELECT w FROM Widget w WHERE w.project.id = :projectId AND w.widgetType IN :widgetTypes AND :contentField MEMBER w.contentFields")
  List<Widget> findAllByProjectIdAndWidgetTypeInAndContentFieldsContains(
      @Param("projectId") Long projectId,
      @Param("widgetTypes") List<String> widgetTypes, @Param("contentField") String contentField);

  @Query(value = "SELECT * FROM widget w JOIN owned_entity se on w.id = se.id JOIN content_field cf on w.id = cf.id "
      + " WHERE se.project_id = :projectId AND w.widget_type IN :widgetTypes AND cf.field LIKE :contentFieldPart || '%'", nativeQuery = true)
  List<Widget> findAllByProjectIdAndWidgetTypeInAndContentFieldContaining(@Param("projectId") Long projectId,
      @Param("widgetTypes") List<String> widgetTypes, @Param("contentFieldPart") String contentFieldPart);
}
