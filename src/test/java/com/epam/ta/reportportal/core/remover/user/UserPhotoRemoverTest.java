package com.epam.ta.reportportal.core.remover.user;

import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class UserPhotoRemoverTest {
	private static final String USER_PHOTO_ID = "SOME_PHOTO_ID";
	private static final String USER_THUMBNAIL_ID = "SOME_THUMBNAIL_ID";
	private static final Long ATTACHMENT_PHOTO_ID = 1L;
	private static final Long ATTACHMENT_THUMBNAIL_ID = 2L;

	private final AttachmentRepository attachmentRepository = mock(AttachmentRepository.class);
	private final UserPhotoRemover userPhotoRemover = new UserPhotoRemover(attachmentRepository);

	@Test
	public void removePhotoWithoutPhotoAttachmentTest() {
		final User user = mock(User.class);

		userPhotoRemover.remove(user);

		verifyNoInteractions(attachmentRepository);
	}

	@Test
	public void removePhotoWithoutThumbnailAttachmentTest() {
		final User user = mock(User.class);
		final Attachment userPhoto = mock(Attachment.class);

		when(user.getAttachment()).thenReturn(USER_PHOTO_ID);

		when(userPhoto.getId()).thenReturn(ATTACHMENT_PHOTO_ID);
		when(userPhoto.getFileId()).thenReturn(USER_PHOTO_ID);

		doReturn(userPhoto).when(attachmentRepository).save(argThat(argument -> argument.getFileId().equals(USER_PHOTO_ID)));

		userPhotoRemover.remove(user);

		verify(attachmentRepository, times(1)).save(any(Attachment.class));
		verify(attachmentRepository, times(1)).moveForDeletion(eq(List.of(ATTACHMENT_PHOTO_ID)));
	}

	@Test
	public void removePhotoTest() {
		final User user = mock(User.class);
		final Attachment userPhoto = mock(Attachment.class);
		final Attachment userThumbnail = mock(Attachment.class);

		when(user.getAttachment()).thenReturn(USER_PHOTO_ID);
		when(user.getAttachmentThumbnail()).thenReturn(USER_THUMBNAIL_ID);

		when(userPhoto.getId()).thenReturn(ATTACHMENT_PHOTO_ID);
		when(userPhoto.getFileId()).thenReturn(USER_PHOTO_ID);
		when(userThumbnail.getId()).thenReturn(ATTACHMENT_THUMBNAIL_ID);
		when(userThumbnail.getFileId()).thenReturn(USER_THUMBNAIL_ID);

		doReturn(userPhoto).when(attachmentRepository).save(argThat(argument -> argument.getFileId().equals(USER_PHOTO_ID)));
		doReturn(userThumbnail).when(attachmentRepository).save(argThat(argument -> argument.getFileId().equals(USER_THUMBNAIL_ID)));

		userPhotoRemover.remove(user);

		verify(attachmentRepository, times(2)).save(any(Attachment.class));
		verify(attachmentRepository, times(1)).moveForDeletion(eq(List.of(ATTACHMENT_PHOTO_ID, ATTACHMENT_THUMBNAIL_ID)));
	}
}
