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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ProjectActivityConverterTest {

  @Test
  void toActivityResource() {
    final Project project = getProject();
    final ProjectAttributesActivityResource resource = ProjectActivityConverter.TO_ACTIVITY_RESOURCE.apply(
        project);

    assertEquals(resource.getProjectId(), project.getId());
    assertEquals(resource.getProjectName(), project.getName());
    assertThat(resource.getConfig()).containsOnlyKeys("attr.lol");
    assertThat(resource.getConfig()).containsValue("value");

  }

  private static Project getProject() {
    Project project = new Project();
    project.setId(1L);
    project.setName("name");
    final Attribute attribute = new Attribute();
    attribute.setId(2L);
    attribute.setName("attr.lol");
    final ProjectAttribute projectAttribute = new ProjectAttribute().withProject(project)
        .withValue("value").withAttribute(attribute);
    project.setProjectAttributes(Sets.newHashSet(projectAttribute));
    return project;
  }
}