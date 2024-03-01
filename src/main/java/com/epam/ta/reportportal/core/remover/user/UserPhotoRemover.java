/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.remover.user;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@Service
public class UserPhotoRemover implements ContentRemover<User> {

  private static final String ATTACHMENT_CONTENT_TYPE = "attachmentContentType";

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
    attachment.setCreationDate(LocalDateTime.now());
    return attachmentRepository.save(attachment).getId();
  }
}
