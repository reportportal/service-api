package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Narrow DB-only persistence step for {@link TmsAttachment}, kept as a separate bean so its {@link Transactional}
 * boundary never wraps blob storage IO (see {@link TmsAttachmentServiceImpl}).
 */
@Service
@RequiredArgsConstructor
public class TmsAttachmentPersistenceService {

  private final TmsAttachmentRepository tmsAttachmentRepository;

  /**
   * Persists the attachment within its own short-lived transaction, forcing commit.
   */
  @Transactional
  public TmsAttachment persist(TmsAttachment attachment) {
    return tmsAttachmentRepository.save(attachment);
  }
}
