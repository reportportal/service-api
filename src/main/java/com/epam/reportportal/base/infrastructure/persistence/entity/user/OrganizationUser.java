/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.user;

import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Join of a user to an organization and org-level role.
 *
 * @author Siarhei Hrabko
 */
@Entity
@Table(name = "organization_user", schema = "public")
@Setter
@Getter
public class OrganizationUser implements Serializable {

  @EmbeddedId
  private OrganizationUserId id = new OrganizationUserId();

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("organizationId")
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userId")
  private User user;

  @Column(name = "organization_role")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private OrganizationRole organizationRole;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrganizationUser)) {
      return false;
    }
    OrganizationUser that = (OrganizationUser) o;
    return organization.equals(that.organization)
        && id.equals(that.id)
        && user.equals(that.user)
        && organizationRole == that.organizationRole;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, organization, user, organizationRole);
  }

}
