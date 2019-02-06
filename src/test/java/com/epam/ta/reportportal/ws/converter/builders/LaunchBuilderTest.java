package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class LaunchBuilderTest {

	@Test
	public void launchBuilder() {
		final String description = "description";
		final LocalDateTime now = LocalDateTime.now();
		final Date date = Date.from(now.atZone(ZoneId.of("UTC")).toInstant());
		final Long projectId = 1L;
		final ItemAttributeResource attributeResource = new ItemAttributeResource("key", "value");
		final Long userId = 2L;
		final String passed = "PASSED";
		final Mode mode = Mode.DEFAULT;

		final Launch launch = new LaunchBuilder().addDescription(description)
				.addEndTime(date)
				.addProject(projectId)
				.addAttribute(attributeResource)
				.addUser(userId)
				.addStatus(passed)
				.addMode(mode)
				.get();

		assertEquals(description, launch.getDescription());
		assertEquals(now, launch.getEndTime());
		assertEquals(projectId, launch.getProjectId());
		assertTrue(launch.getAttributes().contains(new ItemAttribute("key", "value", false)));
		assertEquals(userId, launch.getUser().getId());
		assertEquals(passed, launch.getStatus().name());
		assertEquals(LaunchModeEnum.DEFAULT, launch.getMode());
	}

	@Test
	public void startRq() {
		final StartLaunchRQ request = new StartLaunchRQ();
		final String uuid = "uuid";
		request.setUuid(uuid);
		request.setMode(Mode.DEFAULT);
		final String description = "description";
		request.setDescription(description);
		final String name = "name";
		request.setName(name);
		final LocalDateTime now = LocalDateTime.now();
		request.setStartTime(Date.from(now.atZone(ZoneId.of("UTC")).toInstant()));
		request.setAttributes(Sets.newHashSet(new ItemAttributeResource("key", "value")));

		final Launch launch = new LaunchBuilder().addStartRQ(request).addAttributes(request.getAttributes()).get();

		assertEquals(name, launch.getName());
		assertEquals(uuid, launch.getUuid());
		assertEquals(description, launch.getDescription());
		assertEquals(now, launch.getStartTime());
		assertTrue(launch.getAttributes().contains(new ItemAttribute("key", "value", false)));
		assertEquals(LaunchModeEnum.DEFAULT, launch.getMode());
	}

	@Test
	public void overWriteAttributes() {
		Launch launch = new Launch();
		final ItemAttribute systemAttribute = new ItemAttribute("key", "value", true);
		launch.setAttributes(Sets.newHashSet(new ItemAttribute("key", "value", false), systemAttribute));

		final Launch buildLaunch = new LaunchBuilder(launch).overwriteAttributes(Sets.newHashSet(new ItemAttributeResource("newKey",
				"newVal"
		))).get();

		assertThat(buildLaunch.getAttributes()).containsExactlyInAnyOrder(new ItemAttribute("newKey", "newVal", false), systemAttribute);
	}
}