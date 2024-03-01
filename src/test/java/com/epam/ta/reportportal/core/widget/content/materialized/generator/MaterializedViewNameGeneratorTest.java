/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.materialized.generator;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class MaterializedViewNameGeneratorTest {

  private final MaterializedViewNameGenerator generator = new MaterializedViewNameGenerator();

  private static Stream<Arguments> provideData() {
    return Stream.of(Arguments.of("widget_1_1", getWidget(1L, 1L)),
        Arguments.of("widget_1_2", getWidget(2L, 1L)),
        Arguments.of("widget_2_1", getWidget(1L, 2L))
    );
  }

  private static Widget getWidget(Long id, Long projectId) {
    final Widget widget = new Widget();
    widget.setId(id);
    final Project project = new Project();
    project.setId(projectId);
    widget.setProject(project);
    return widget;
  }

  @ParameterizedTest
  @MethodSource("provideData")
  void generate(String expectedName, Widget widget) {
    Assertions.assertEquals(expectedName, generator.generate(widget));
  }

}