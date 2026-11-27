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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.integration.GetIntegrationHandler;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.model.integration.IntegrationResource;
import com.epam.reportportal.base.model.integration.IntegrationTypeResource;
import java.util.List;
import org.junit.jupiter.api.Test;

class IntegrationControllerGlobalListTest {

  private final GetIntegrationHandler getHandler = mock(GetIntegrationHandler.class);
  private final IntegrationController controller =
      new IntegrationController(null, null, getHandler, null, null);

  private static IntegrationResource resource(long id, String group) {
    var type = new IntegrationTypeResource();
    type.setGroupType(group);
    var resource = new IntegrationResource();
    resource.setId(id);
    resource.setIntegrationType(type);
    return resource;
  }

  private List<IntegrationResource> all() {
    return List.of(resource(1, "AUTH"), resource(2, "NOTIFICATION"), resource(3, "BTS"));
  }

  @Test
  void nonAdminDoesNotSeeAuthIntegrations() {
    when(getHandler.getGlobalIntegrations()).thenReturn(all());
    when(getHandler.getGlobalIntegrations("ldap")).thenReturn(all());
    var user = getRpUser("u", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    assertThat(controller.getGlobalIntegrations(user)).extracting(IntegrationResource::getId)
        .containsExactly(2L, 3L);
    assertThat(controller.getGlobalIntegrations(user, "ldap")).extracting(IntegrationResource::getId)
        .containsExactly(2L, 3L);
  }

  @Test
  void adminSeesEverything() {
    when(getHandler.getGlobalIntegrations()).thenReturn(all());
    var admin = getRpUser("a", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    assertThat(controller.getGlobalIntegrations(admin)).hasSize(3);
  }
}
