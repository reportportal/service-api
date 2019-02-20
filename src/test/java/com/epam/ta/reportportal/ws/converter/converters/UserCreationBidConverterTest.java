package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class UserCreationBidConverterTest {

	@Test
	public void toUser() {
		CreateUserRQ request = new CreateUserRQ();
		final String email = "email@example.com";
		request.setEmail(email);
		final String role = "role";
		request.setRole(role);
		final Project project = new Project();
		project.setName("projectName");
		final Date creationDate = Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant());
		project.setCreationDate(creationDate);

		final UserCreationBid bid = UserCreationBidConverter.TO_USER.apply(request, project);

		assertNotNull(bid.getUuid());
		assertEquals(bid.getEmail(), email);
		assertEquals(bid.getRole(), role);
		assertEquals(bid.getDefaultProject(), project);
	}
}