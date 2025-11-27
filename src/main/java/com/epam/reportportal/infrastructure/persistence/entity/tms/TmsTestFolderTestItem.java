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

package com.epam.reportportal.infrastructure.persistence.entity.tms;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Junction entity representing many-to-many relationship between TMS test folder and test items
 * (SUITE type items in manual launches).
 * <p>
 * This allows tracking which test folder a SUITE item represents within a launch context.
 *
 * @author ReportPortal
 */
@Entity
@Table(
    name = "tms_test_folder_test_item"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestFolderTestItem implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "test_folder_id")
  private Long testFolderId;

  @Column(name = "launch_id")
  private Long launchId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_item_id", nullable = false)
  private TestItem testItem;
}
