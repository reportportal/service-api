package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class UserBuilderTest {

	@Test
	public void userBuilder() {
		final CreateUserRQConfirm request = new CreateUserRQConfirm();
		final String login = "login";
		request.setLogin(login);
		final String email = "email@domain.com";
		request.setEmail(email);
		final String fullName = "full name";
		request.setFullName(fullName);
		request.setPassword("password");
		final UserRole role = UserRole.USER;

		final User user = new UserBuilder().addCreateUserRQ(request).addUserRole(role).get();

		assertEquals(login, user.getLogin());
		assertEquals(email, user.getEmail());
		assertEquals(fullName, user.getFullName());
		assertNotNull(user.getPassword());
		assertEquals(role, user.getRole());
		assertEquals(UserType.INTERNAL, user.getUserType());
		assertNotNull(user.getMetadata());
		assertFalse(user.isExpired());
	}
}