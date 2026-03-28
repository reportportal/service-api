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

package com.epam.reportportal.base.infrastructure.persistence.entity.oauth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@Entity
@Table(name = "oauth_registration_scope", schema = "public")
public class OAuthRegistrationScope implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "scope")
  private String scope;

  @ManyToOne
  @JoinColumn(name = "oauth_registration_fk")
  private OAuthRegistration registration;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuthRegistrationScope that = (OAuthRegistrationScope) o;
    return Objects.equals(scope, that.scope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope);
  }
}
