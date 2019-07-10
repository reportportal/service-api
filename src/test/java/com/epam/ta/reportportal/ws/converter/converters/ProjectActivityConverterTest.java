package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ProjectActivityConverterTest {

	@Test
	void toActivityResource() {
		final Project project = getProject();
		final ProjectAttributesActivityResource resource = ProjectActivityConverter.TO_ACTIVITY_RESOURCE.apply(project);

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
		final ProjectAttribute projectAttribute = new ProjectAttribute().withProject(project).withValue("value").withAttribute(attribute);
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));
		return project;
	}
}