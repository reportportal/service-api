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

package com.epam.reportportal.auth.integration;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.integration.converter.SamlConverter;
import com.epam.reportportal.auth.model.SamlResource;
import com.epam.reportportal.base.infrastructure.model.integration.auth.AbstractAuthResource;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;

/**
 * @author Andrei Varabyeu
 */
@Getter
public enum AuthIntegrationType {

  SAML("saml") {
    @Override
    public Optional<Integration> get(Integration entity) {
      return ofNullable(entity);
    }

    @Override
    public Function<Integration, SamlResource> getToResourceMapper() {
      return SamlConverter.TO_RESOURCE;
    }
  };

  private final String name;

  AuthIntegrationType(String name) {
    this.name = name;
  }

  public abstract Optional<Integration> get(Integration entity);

  public abstract Function<Integration, ? extends AbstractAuthResource> getToResourceMapper();

  public static Optional<AuthIntegrationType> fromId(String id) {
    return Arrays.stream(values()).filter(it -> it.name.equalsIgnoreCase(id)).findAny();
  }

  @Override
  public String toString() {
    return this.name;
  }
}
