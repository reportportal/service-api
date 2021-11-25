/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.project.config;

import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ProjectConfigProviderTest {

	private final GetProjectHandler getProjectHandler = mock(GetProjectHandler.class);
	private final ProjectConfigProvider provider = new ProjectConfigProvider(getProjectHandler);

	@Test
	void shouldReturnConfig() {

		final long projectId = 1L;
		final Project project = new Project();
		project.setId(projectId);
		final Set<ProjectAttribute> projectAttributes = LongStream.range(1, 10).mapToObj(it -> {
			final Attribute attribute = new Attribute(it, String.valueOf(it));
			return new ProjectAttribute(attribute, "Value " + it, project);
		}).collect(Collectors.toSet());
		project.setProjectAttributes(projectAttributes);

		when(getProjectHandler.get(projectId)).thenReturn(project);

		final Map<String, String> attributesMapping = provider.provide(projectId);

		assertEquals(projectAttributes.size(), attributesMapping.size());
		projectAttributes.forEach(a -> {
			final String value = attributesMapping.get(a.getAttribute().getName());
			assertEquals(a.getValue(), value);
		});
	}

}