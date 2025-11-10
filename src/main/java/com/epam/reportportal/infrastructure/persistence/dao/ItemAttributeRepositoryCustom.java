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

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.item.ItemAttributePojo;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface ItemAttributeRepositoryCustom {

  /**
   * Retrieves {@link Launch} and {@link com.epam.reportportal.infrastructure.persistence.entity.item.TestItem}
   * {@link ItemAttribute#getKey()} by project id and part of the {@link ItemAttribute#getKey()}.
   *
   * @param launchFilter   {@link Queryable} with
   *                       {@link
   *                       com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget#LAUNCH_TARGET}
   * @param launchPageable {@link Pageable} for launches sorting and limitation
   * @param isLatest       Flag defines whether all or latest launches launches will be included in the query condition
   * @param keyPart        Part of the {@link ItemAttribute#getKey()}
   * @param isSystem       {@link ItemAttribute#isSystem()}
   * @return {@link List} of matched attribute keys
   */
  List<String> findAllKeysByLaunchFilter(Queryable launchFilter, Pageable launchPageable,
      boolean isLatest, String keyPart,
      boolean isSystem);

  /**
   * Retrieves launch attribute keys by project and part of value. Used for autocompletion functionality
   *
   * @param projectId Id of project
   * @param value     part of key
   * @return List of matched attribute keys
   */
  List<String> findLaunchAttributeKeys(Long projectId, String value, boolean system);

  /**
   * Retrieves launch attribute values by project, specified key and part of value. Used for autocompletion
   * functionality
   *
   * @param projectId Id of project
   * @param key       Specified key
   * @param value     Part of value
   * @return List of matched attribute values
   */
  List<String> findLaunchAttributeValues(Long projectId, String key, String value, boolean system);

  /**
   * Finds all unique attribute keys that contain the specified part of the key (`keyPart`) for a given project or a
   * specific launch.
   *
   * @param projectId the ID of the project within which the search will be conducted; this parameter is mandatory and
   *                  cannot be null.
   * @param keyPart   the part of the attribute key to search for; only keys containing this substring will be included
   *                  in the results. Cannot be null or empty.
   * @param launchId  the ID of the specific launch to restrict the search to. If null, the search will include all
   *                  launches within the project.
   * @param system    determines whether to filter the results by system attributes: - {@code true} to include only
   *                  system attributes. - {@code false} to include only non-system attributes.
   * @return a list of unique attribute keys containing `keyPart` as a substring. If no keys are found matching the
   * criteria, an empty list is returned.
   * @throws IllegalArgumentException if `projectId` is null or if `keyPart` is null or empty.
   */
  List<String> findUniqueAttributeKeysByPart(Long projectId, String keyPart, Long launchId,
      boolean system);

  /**
   * Finds all unique attribute values that contain the specified part of the value (`valuePart`) for a given project or
   * a specific launch.
   *
   * @param projectId the ID of the project within which the search will be conducted; this parameter is mandatory and
   *                  cannot be null.
   * @param valuePart the part of the attribute value to search for; only values containing this substring will be
   *                  included in the results. Cannot be null or empty.
   * @param launchId  the ID of the specific launch to restrict the search to. If null, the search will include all
   *                  launches within the project.
   * @param system    determines whether to filter the results by system attributes: - {@code true} to include only
   *                  system attributes. - {@code false} to include only non-system attributes.
   * @return a list of unique attribute values containing `valuePart` as a substring. If no values are found matching
   * the criteria, an empty list is returned.
   * @throws IllegalArgumentException if `projectId` is null or if `valuePart` is null or empty.
   */
  List<String> findUniqueAttributeValuesByPart(Long projectId, String key, String valuePart,
      Long launchId, boolean system);

  /**
   * Retrieves test item attribute values by launch, specified key and part of value. Used for autocompletion
   * functionality
   *
   * @param launchId Id of launch
   * @param key      Specified key
   * @param value    Part of value
   * @return List of matched attribute values
   */
  List<String> findTestItemAttributeValues(Long launchId, String key, String value, boolean system);

  /**
   * Retrieves test item attribute keys by project id and part of value. Used for autocompletion functionality
   *
   * @param projectId  Id of {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} which
   *                   items' attribute keys should be found
   * @param launchName {@link Launch#getName()} which items' attributes should be found
   * @param keyPart    part of key
   * @return List of matched attribute keys
   */
  List<String> findTestItemKeysByProjectIdAndLaunchName(Long projectId, String launchName,
      String keyPart, boolean system);

  /**
   * Retrieves test item attribute values by project id, specified key and part of value. Used for autocompletion
   * functionality
   *
   * @param projectId  Id of {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project} which
   *                   items' attribute values should be found
   * @param launchName {@link Launch#getName()} which items' attributes should be found
   * @param key        Specified key
   * @param valuePart  Part of value
   * @return List of matched attribute values
   */
  List<String> findTestItemValuesByProjectIdAndLaunchName(Long projectId, String launchName,
      String key, String valuePart,
      boolean system);

  /**
   * Save item attribute by {@link com.epam.reportportal.infrastructure.persistence.entity.item.TestItem#itemId}
   *
   * @param itemId   {@link ItemAttribute#testItem} ID
   * @param key      {@link ItemAttribute#key}
   * @param value    {@link ItemAttribute#value}
   * @param isSystem {@link ItemAttribute#system}
   * @return 1 if inserted, otherwise 0
   */
  int saveByItemId(Long itemId, String key, String value, boolean isSystem);

  /**
   * Save item attribute by {@link Launch#getId()}
   *
   * @return 1 if inserted, otherwise 0
   */
  int saveByLaunchId(Long launchId, String key, String value, boolean isSystem);

  /**
   * Method for batch inserting of the {@link ItemAttribute}. Used for performance improvement
   *
   * @param itemAttributes The {@link List} of the {@link ItemAttributePojo}
   * @return Number of inserted records
   */
  int saveMultiple(List<ItemAttributePojo> itemAttributes);
}
