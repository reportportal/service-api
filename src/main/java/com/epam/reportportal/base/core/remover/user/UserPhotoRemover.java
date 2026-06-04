/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.remover.user;

import static com.epam.reportportal.base.infrastructure.persistence.binary.impl.DataStoreUtils.ATTACHMENT_CONTENT_TYPE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.remover.ContentRemover;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Removes the profile photo attachment of a
 * {@link com.epam.reportportal.base.infrastructure.persistence.entity.user.User} when the user is deleted.
 *
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class UserPhotoRemover implements ContentRemover<User> {

  private final AttachmentRepository attachmentRepository;

  @Autowired
  public UserPhotoRemover(AttachmentRepository attachmentRepository) {
    this.attachmentRepository = attachmentRepository;
  }

  @Override
  public void remove(User user) {
    ofNullable(user.getAttachment()).ifPresent(fileId -> {
      List<Long> attachmentsIds = new ArrayList<>(2);
      attachmentsIds.add(prepareAttachmentAndGetId(fileId));
      Optional.ofNullable(user.getAttachmentThumbnail())
          .ifPresent(thumbnailId -> attachmentsIds.add(prepareAttachmentAndGetId(thumbnailId)));
      ofNullable(user.getMetadata()).ifPresent(
          metadata -> metadata.getMetadata().remove(ATTACHMENT_CONTENT_TYPE));
      attachmentRepository.moveForDeletion(attachmentsIds);
    });
  }

  private Long prepareAttachmentAndGetId(String fileId) {
    Attachment attachment = new Attachment();
    attachment.setFileId(fileId);
    attachment.setCreationDate(Instant.now());
    return attachmentRepository.save(attachment).getId();
  }
}
