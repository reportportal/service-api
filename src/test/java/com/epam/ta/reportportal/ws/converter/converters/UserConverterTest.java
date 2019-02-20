package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.enums.ProjectType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserConverterTest {

	@Test
	void toResource() {
		final User user = getUser();
		final UserResource resource = UserConverter.TO_RESOURCE.apply(user);

		assertEquals(resource.getEmail(), user.getEmail());
		assertEquals(resource.getFullName(), user.getFullName());
		assertEquals(resource.getUserId(), user.getLogin());
		assertEquals(resource.getUserRole(), user.getRole().name());
		assertEquals(resource.getAccountType(), user.getUserType().name());
		assertEquals(resource.getPhotoId(), user.getAttachment());
		assertThat((HashMap<String, Object>) resource.getMetadata()).containsAllEntriesOf(getMetadata());
		assertThat(resource.getAssignedProjects()).containsKeys("project1", "project2");
	}

	@Test
	void toActivityResource() {
		final User user = getUser();
		final Long projectId = 2L;
		final UserActivityResource resource = UserConverter.TO_ACTIVITY_RESOURCE.apply(user, projectId);

		assertEquals(resource.getId(), user.getId());
		assertEquals(resource.getFullName(), user.getFullName());
		assertEquals(resource.getDefaultProjectId(), projectId);
	}

	private static User getUser() {
		final User user = new User();
		user.setLogin("login");
		user.setUserType(UserType.INTERNAL);
		user.setRole(UserRole.USER);
		user.setAttachment("attachmentId");
		user.setEmail("example@domain.com");
		user.setFullName("full name");
		user.setId(1L);
		user.setAttachmentThumbnail("thumbnailId");
		user.setExpired(false);
		final HashMap<String, Object> metadata = getMetadata();
		user.setMetadata(new Metadata(metadata));
		final Project project1 = new Project();
		project1.setName("project1");
		project1.setProjectType(ProjectType.INTERNAL);
		final ProjectUser projectUser1 = new ProjectUser().withProject(project1).withProjectRole(ProjectRole.MEMBER).withUser(user);
		final Project project2 = new Project();
		project2.setName("project2");
		project2.setProjectType(ProjectType.INTERNAL);
		final ProjectUser projectUser2 = new ProjectUser().withProject(project2)
				.withProjectRole(ProjectRole.PROJECT_MANAGER)
				.withUser(user);
		user.setProjects(Sets.newHashSet(projectUser1, projectUser2));
		return user;
	}

	private static HashMap<String, Object> getMetadata() {
		final HashMap<String, Object> metadata = new HashMap<>();
		metadata.put("key1", "value1");
		metadata.put("key2", "value2");
		return metadata;
	}
}