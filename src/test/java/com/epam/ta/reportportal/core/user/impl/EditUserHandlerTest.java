package com.epam.ta.reportportal.core.user.impl;

import static com.epam.ta.reportportal.database.entity.project.EntryType.UPSA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;

public class EditUserHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void deletePhotoForExternalUser() {
		String login = "login";
		EditUserHandler editUserHandler = new EditUserHandler();
		UserRepository userRepository = mock(UserRepository.class);
		User user = new User();
		user.setEntryType(UPSA);
		when(userRepository.findOne(login)).thenReturn(user);
		editUserHandler.setUserRepository(userRepository);

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions. Unable to change photo for external user");
		editUserHandler.deletePhoto(login);
	}
}