package com.epam.ta.reportportal.core.remover.user;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.entity.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class UserContentRemoverTest {

	private final UserPhotoRemover userPhotoRemover = mock(UserPhotoRemover.class);
	private final UserWidgetRemover userWidgetRemover = mock(UserWidgetRemover.class);
	private final ContentRemover<User> userContentRemover = new UserContentRemover(List.of(userWidgetRemover, userPhotoRemover));

	@Test
	public void removeTest() {
		User user = mock(User.class);

		userContentRemover.remove(user);

		verify(userWidgetRemover, times(1)).remove(eq(user));
		verify(userPhotoRemover, times(1)).remove(eq(user));
	}

}
