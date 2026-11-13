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

package com.epam.reportportal.base.core.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@ExtendWith(MockitoExtension.class)
public class PersonalOrganizationServiceTest {

  @Mock
  private Pf4jPluginBox pluginBox;

  @InjectMocks
  private PersonalOrganizationService personalOrganizationService;

  @Test
  public void pluginNotFound() {
    when(pluginBox.getInstance(OrganizationExtensionPoint.class)).thenReturn(Optional.empty());
    Optional<OrganizationInfo> result = personalOrganizationService.createPersonalOrganization(0L);

    assertEquals(Optional.empty(), result);
  }

  @Test
  public void pluginThrowsException() {
    when(pluginBox.getInstance(OrganizationExtensionPoint.class)).thenThrow(new RuntimeException("Test exception"));
    Optional<OrganizationInfo> result = personalOrganizationService.createPersonalOrganization(0L);

    assertEquals(Optional.empty(), result);
  }

}
