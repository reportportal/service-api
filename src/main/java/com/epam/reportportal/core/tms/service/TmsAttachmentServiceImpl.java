package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.core.tms.mapper.TmsAttachmentMapper;
import com.epam.reportportal.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsManualScenarioPreconditionsAttachmentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsStepAttachmentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTextManualScenarioAttachmentRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsAttachmentServiceImpl implements TmsAttachmentService {

  private final TmsAttachmentRepository tmsAttachmentRepository;
  private final TmsAttachmentDataStoreService tmsAttachmentDataStoreService;
  private final TmsAttachmentMapper tmsAttachmentMapper;
  private final TmsStepAttachmentRepository tmsStepAttachmentRepository;
  private final TmsTextManualScenarioAttachmentRepository tmsTextManualScenarioAttachmentRepository;
  private final TmsManualScenarioPreconditionsAttachmentRepository tmsManualScenarioPreconditionsAttachmentRepository;

  @Value("${rp.tms.attachment.ttl:PT24H}")
  private Duration ttl;

  @Override
  @Transactional
  public UploadAttachmentRS uploadAttachment(MultipartFile file) {
    if (file.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "File cannot be empty");
    }

    try {
      var fileId = tmsAttachmentDataStoreService.save(file.getOriginalFilename(),
          file.getInputStream());

      var attachment = tmsAttachmentMapper.convertToAttachment(fileId, file);

      return tmsAttachmentMapper.convertToUploadAttachmentRS(
          tmsAttachmentRepository.save(attachment));
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
          "Failed to upload attachment: " + e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TmsAttachment> getTmsAttachment(Long attachmentId) {
    return tmsAttachmentRepository.findById(attachmentId);
  }

  @Override
  @Transactional
  public void deleteAttachment(Long attachmentId) {
    var attachment = tmsAttachmentRepository
        .findById(attachmentId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Attachment not found: " + attachmentId));

    try {
      // Delete file from data store
      tmsAttachmentDataStoreService.delete(attachment.getPathToFile());

      // Delete attachment record
      tmsAttachmentRepository.deleteById(attachmentId);
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
          "Failed to delete attachment: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public void removeTtlFromTmsAttachments(List<Long> attachmentIds) {
    if (CollectionUtils.isNotEmpty(attachmentIds)) {
      tmsAttachmentRepository.removeExpirationFromAttachments(attachmentIds);
    }
  }

  @Override
  @Transactional
  public void cleanupExpiredAttachments() {
    var expiredAttachments = tmsAttachmentRepository.findExpiredAttachments(Instant.now());

    if (!expiredAttachments.isEmpty()) {
      log.info("Found {} expired attachments for cleanup", expiredAttachments.size());

      // Delete files from data store
      expiredAttachments.forEach(attachment -> {
        try {
          tmsAttachmentDataStoreService.delete(attachment.getPathToFile());
        } catch (Exception e) {
          log.warn("Failed to delete file {} for expired attachment {}: {}",
              attachment.getPathToFile(), attachment.getId(), e.getMessage());
        }
      });

      // Delete attachment records
      var expiredIds = expiredAttachments
          .stream()
          .map(TmsAttachment::getId)
          .collect(Collectors.toList());
      tmsAttachmentRepository.deleteByIds(expiredIds);

      log.info("Cleaned up {} expired attachments", expiredIds.size());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsAttachment> getTmsAttachmentsByIds(List<Long> attachmentIds) {
    if (CollectionUtils.isEmpty(attachmentIds)) {
      return List.of();
    }

    return tmsAttachmentRepository.findAllById(attachmentIds);
  }

  @Override
  @Transactional
  public TmsAttachment duplicateTmsAttachment(TmsAttachment originalAttachment) {
    log.debug("Duplicating TMS attachment with ID: {}", originalAttachment.getId());

    try {
      // Step 1: Load original file from data store
      var originalFileStream = tmsAttachmentDataStoreService.load(originalAttachment.getPathToFile())
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
              "Original attachment file not found: " + originalAttachment.getPathToFile()));

      // Step 2: Generate new unique filename to avoid conflicts
      var newFileName = generateDuplicateFileName(originalAttachment.getFileName());

      // Step 3: Save file copy to data store
      var newFileId = tmsAttachmentDataStoreService.save(newFileName, originalFileStream);

      // Step 4: Create duplicated attachment entity
      var duplicatedAttachment = tmsAttachmentMapper.duplicateAttachment(originalAttachment,
          newFileId);

      // Note: TTL is not set, making duplicated attachment permanent by default
      // Note: Entity relationships (step, textManualScenario, etc.) are not copied
      // They should be set by the calling service based on context

      // Step 5: Save duplicated attachment to a database
      var savedDuplicate = tmsAttachmentRepository.save(duplicatedAttachment);

      log.info("Successfully duplicated TMS attachment {} to new attachment {} with file: {}",
          originalAttachment.getId(), savedDuplicate.getId(), newFileName);

      return savedDuplicate;

    } catch (Exception e) {
      log.error("Failed to duplicate TMS attachment {}: {}", originalAttachment.getId(),
          e.getMessage());
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
          "Failed to duplicate TMS attachment: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public void setExpirationForUnusedAttachments() {
    log.debug("Setting TTL for unused TMS attachments (TTL: {})", ttl);

    // Find all attachments without TTL
    var attachmentsWithoutTtl = tmsAttachmentRepository.findAttachmentsWithoutTtl();

    if (CollectionUtils.isEmpty(attachmentsWithoutTtl)) {
      log.debug("No TMS attachments found without TTL");
      return;
    }

    log.debug("Found {} TMS attachments without TTL", attachmentsWithoutTtl.size());

    // Get all attachment IDs that are referenced in junction tables
    var usedAttachmentIds = getUsedAttachmentIds();

    log.debug("Found {} TMS attachments referenced in junction tables", usedAttachmentIds.size());

    // Filter out used attachments to get only unused ones
    var unusedAttachmentIds = attachmentsWithoutTtl
        .stream()
        .map(TmsAttachment::getId)
        .filter(id -> !usedAttachmentIds.contains(id))
        .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(unusedAttachmentIds)) {
      log.debug("No unused TMS attachments found without TTL");
      return;
    }

    // Set expires_at to configured hours from now
    var expirationTime = Instant.now().plus(ttl);

    var updatedCount = tmsAttachmentRepository.setExpirationForAttachments(
        unusedAttachmentIds, expirationTime
    );

    log.info("Set TTL (expires at: {}) for {} unused TMS attachments",
        expirationTime, updatedCount);
  }

  @Override
  @Transactional
  public void saveAll(Collection<TmsAttachment> attachments) {
    if (CollectionUtils.isEmpty(attachments)) {
      return;
    }
    tmsAttachmentRepository.saveAll(attachments);
  }

  private Set<Long> getUsedAttachmentIds() {
    // Get attachment IDs from step attachments
    var stepAttachmentIds = tmsStepAttachmentRepository.findAllAttachmentIds();
    var usedIds = new HashSet<>(stepAttachmentIds);
    log.debug("Found {} attachment IDs in step attachments", stepAttachmentIds.size());

    // Get attachment IDs from text manual scenario attachments
    var textScenarioAttachmentIds = tmsTextManualScenarioAttachmentRepository.findAllAttachmentIds();
    usedIds.addAll(textScenarioAttachmentIds);
    log.debug("Found {} attachment IDs in text manual scenario attachments", textScenarioAttachmentIds.size());

    // Get attachment IDs from preconditions attachments
    var preconditionsAttachmentIds = tmsManualScenarioPreconditionsAttachmentRepository.findAllAttachmentIds();
    usedIds.addAll(preconditionsAttachmentIds);
    log.debug("Found {} attachment IDs in preconditions attachments", preconditionsAttachmentIds.size());

    log.debug("Total unique attachment IDs in use: {}", usedIds.size());
    return usedIds;
  }

  /**
   * Generates a unique filename for duplicated attachment. * Adds timestamp and UUID to avoid
   * filename conflicts. * * @param originalFileName the original filename * @return new unique
   * filename
   */
  private String generateDuplicateFileName(String originalFileName) {
    if (originalFileName == null || originalFileName.trim().isEmpty()) {
      return "duplicated_attachment_" + System.currentTimeMillis() + "_" + UUID.randomUUID()
          .toString().substring(0, 8);
    }

    var baseName = FilenameUtils.getBaseName(originalFileName);
    var extension = FilenameUtils.getExtension(originalFileName);
    var timestamp = System.currentTimeMillis();
    var uniqueId = UUID.randomUUID().toString().substring(0, 8);

    if (!extension.isEmpty()) {
      return String.format("%s_copy_%d_%s.%s", baseName, timestamp, uniqueId, extension);
    } else {
      return String.format("%s_copy_%d_%s", baseName, timestamp, uniqueId);
    }
  }
}
