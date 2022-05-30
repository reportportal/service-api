package com.epam.ta.reportportal.core.remover.user;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class UserPhotoRemover implements ContentRemover<User> {
	private static final String ATTACHMENT_CONTENT_TYPE = "attachmentContentType";

	private final AttachmentRepository attachmentRepository;

	@Autowired
	public UserPhotoRemover(AttachmentRepository attachmentRepository) {
		this.attachmentRepository = attachmentRepository;
	}

	private Long prepareAttachmentAndGetId(String fileId) {
		Attachment attachment = new Attachment();
		attachment.setFileId(fileId);
		attachment.setCreationDate(LocalDateTime.now());
		return attachmentRepository.save(attachment).getId();
	}

	@Override
	public void remove(User user) {
		ofNullable(user.getAttachment()).ifPresent(fileId -> {
			List<Long> attachmentsIds = new ArrayList<>(2);
			attachmentsIds.add(prepareAttachmentAndGetId(fileId));
			user.setAttachment(null);
			Optional.ofNullable(user.getAttachmentThumbnail()).ifPresent(thumbnailId -> {
				attachmentsIds.add(prepareAttachmentAndGetId(thumbnailId));
				user.setAttachmentThumbnail(null);
			});
			ofNullable(user.getMetadata()).ifPresent(metadata -> metadata.getMetadata().remove(ATTACHMENT_CONTENT_TYPE));
			attachmentRepository.moveForDeletion(attachmentsIds);
		});
	}
}
