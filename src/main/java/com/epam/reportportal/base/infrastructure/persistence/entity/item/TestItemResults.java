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

package com.epam.reportportal.base.infrastructure.persistence.entity.item;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


/**
 * Execution outcome for a test item: status, end time, duration, issue, and statistics.
 *
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "test_item_results", schema = "public")
@Setter
@Getter
@NoArgsConstructor
public class TestItemResults implements Serializable {

  @Id
  @Column(name = "result_id", unique = true, nullable = false, precision = 64)
  private Long itemId;

  @Column(name = "status", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private StatusEnum status;

  @Column(name = "end_time")
  private Instant endTime;

  @Column(name = "duration")
  private Double duration;

  @PrimaryKeyJoinColumn
  @OneToOne(mappedBy = "testItemResults", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
      CascadeType.REMOVE})
  private IssueEntity issue;

  @OneToMany
  @JoinColumn(name = "item_id", insertable = false, updatable = false)
  @Fetch(FetchMode.SUBSELECT)
  private Set<Statistics> statistics = Sets.newHashSet();

  @OneToOne
  @MapsId
  @JoinColumn(name = "result_id")
  private TestItem testItem;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestItemResults that = (TestItemResults) o;
    return Objects.equals(itemId, that.itemId) && status == that.status && Objects.equals(endTime,
        that.endTime) && Objects.equals(
        duration,
        that.duration
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemId, status, endTime, duration);
  }
}
