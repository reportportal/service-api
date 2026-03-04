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

package com.epam.reportportal.auth.integration.converter;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.integration.parameter.LdapParameter;
import com.epam.reportportal.auth.integration.parameter.ParameterUtils;
import com.epam.reportportal.base.infrastructure.model.integration.auth.LdapAttributes;
import com.epam.reportportal.base.infrastructure.model.integration.auth.LdapResource;
import com.epam.reportportal.base.infrastructure.model.integration.auth.SynchronizationAttributesResource;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class LdapConverter {

  private LdapConverter() {
    //static only
  }

  static final Function<Integration, LdapAttributes> LDAP_ATTRIBUTES_TO_RESOURCE =
      ldapIntegration -> {
        LdapAttributes ldapAttributes = new LdapAttributes();
        ldapAttributes.setEnabled(ldapIntegration.isEnabled());
        LdapParameter.BASE_DN.getParameter(ldapIntegration).ifPresent(ldapAttributes::setBaseDn);
        LdapParameter.URL.getParameter(ldapIntegration).ifPresent(ldapAttributes::setUrl);

        SynchronizationAttributesResource attributes = new SynchronizationAttributesResource();
        LdapParameter.EMAIL_ATTRIBUTE.getParameter(ldapIntegration).ifPresent(attributes::setEmail);
        LdapParameter.FULL_NAME_ATTRIBUTE.getParameter(ldapIntegration)
            .ifPresent(attributes::setFullName);
        LdapParameter.FIRST_NAME_ATTRIBUTE.getParameter(ldapIntegration)
            .ifPresent(attributes::setFirstName);
        LdapParameter.LAST_NAME_ATTRIBUTE.getParameter(ldapIntegration)
            .ifPresent(attributes::setLastName);
        LdapParameter.PHOTO_ATTRIBUTE.getParameter(ldapIntegration).ifPresent(attributes::setPhoto);
        ldapAttributes.setSynchronizationAttributes(attributes);
        return ldapAttributes;
      };

  public static final Function<Integration, LdapResource> TO_RESOURCE =
      integration -> {
        LdapResource ldapResource = new LdapResource();
        ldapResource.setId(integration.getId());
        ldapResource.setLdapAttributes(LDAP_ATTRIBUTES_TO_RESOURCE.apply(integration));
        LdapParameter.GROUP_SEARCH_FILTER.getParameter(integration)
            .ifPresent(ldapResource::setGroupSearchFilter);
        LdapParameter.GROUP_SEARCH_BASE.getParameter(integration)
            .ifPresent(ldapResource::setGroupSearchBase);
        LdapParameter.PASSWORD_ATTRIBUTE.getParameter(integration)
            .ifPresent(ldapResource::setPasswordAttribute);
        LdapParameter.PASSWORD_ENCODER_TYPE.getParameter(integration)
            .ifPresent(ldapResource::setPasswordEncoderType);
        LdapParameter.USER_DN_PATTERN.getParameter(integration)
            .ifPresent(ldapResource::setUserDnPattern);
        LdapParameter.USER_SEARCH_FILTER.getParameter(integration)
            .ifPresent(ldapResource::setUserSearchFilter);
        return ldapResource;
      };

  static final BiFunction<LdapAttributes, Integration, Integration> LDAP_ATTRIBUTES_FROM_RESOURCE =
      (attributes, integration) -> {
        ofNullable(attributes.getEnabled()).ifPresent(integration::setEnabled);
        ofNullable(attributes.getBaseDn()).ifPresent(
            it -> LdapParameter.BASE_DN.setParameter(integration, it));
        ofNullable(attributes.getUrl()).ifPresent(
            it -> LdapParameter.URL.setParameter(integration, it));
        ofNullable(attributes.getSynchronizationAttributes()).ifPresent(syncAttr -> {
          ofNullable(syncAttr.getEmail()).ifPresent(
              it -> LdapParameter.EMAIL_ATTRIBUTE.setParameter(integration, it));
          ofNullable(syncAttr.getFullName()).ifPresent(
              it -> LdapParameter.FULL_NAME_ATTRIBUTE.setParameter(integration, it));
          ofNullable(syncAttr.getFirstName()).ifPresent(
              it -> LdapParameter.FIRST_NAME_ATTRIBUTE.setParameter(integration, it));
          ofNullable(syncAttr.getLastName()).ifPresent(
              it -> LdapParameter.LAST_NAME_ATTRIBUTE.setParameter(integration, it));
          ofNullable(syncAttr.getPhoto()).ifPresent(
              it -> LdapParameter.PHOTO_ATTRIBUTE.setParameter(integration, it));
        });
        return integration;
      };

  public static final BiConsumer<UpdateAuthRQ, Integration> UPDATE_FROM_REQUEST =
      (request, integration) -> {
        ParameterUtils.setLdapParameters(request, integration);
        integration.setEnabled(ofNullable(request.getEnabled()).orElse(false));
      };
}
