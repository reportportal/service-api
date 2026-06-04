/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.group;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key for {@link GroupUser} entity.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class GroupUserId implements Serializable {

  @Column(name = "group_id")
  private Long groupId;
  @Column(name = "user_id")
  private Long userId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GroupUserId that = (GroupUserId) o;
    return Objects.equals(groupId, that.groupId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, userId);
  }
}
