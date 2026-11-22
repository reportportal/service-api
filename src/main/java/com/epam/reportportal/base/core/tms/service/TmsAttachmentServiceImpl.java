package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.core.tms.mapper.TmsAttachmentMapper;
import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsAttachmentServiceImpl implements TmsAttachmentService {

  private final TmsAttachmentRepository tmsAttachmentRepository;
  private final TmsAttachmentDataStoreService tmsAttachmentDataStoreService;
  private final TmsAttachmentMapper tmsAttachmentMapper;
  private final TmsAttachmentPersistenceService tmsAttachmentPersistenceService;
  private final PlatformTransactionManager transactionManager;
  private final FeatureFlagHandler featureFlagHandler;

  @Value("${rp.tms.attachment.ttl:PT24H}")
  private Duration ttl;

  @Override
  public UploadAttachmentRS uploadAttachment(Long projectId, MultipartFile file) {
    if (file.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "File cannot be empty");
    }

    var fileId = saveBinary(projectId, file);
    var thumbnailId = saveThumbnailIfImage(projectId, file);

    try {
      var attachment = tmsAttachmentMapper.convertToAttachment(fileId, thumbnailId, file, projectId);
      var saved = tmsAttachmentPersistenceService.persist(attachment);
      return tmsAttachmentMapper.convertToUploadAttachmentRS(saved);
    } catch (Exception _) {
      deleteQuietly(fileId, thumbnailId);
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
          "Failed to persist attachment metadata for project " + projectId);
    }
  }

  private String saveBinary(Long projectId, MultipartFile file) {
    try (var in = file.getInputStream()) {
      var storageKey = buildStorageKey(projectId, file.getOriginalFilename());
      return tmsAttachmentDataStoreService.save(storageKey, in);
    } catch (IOException | RuntimeException _) {
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED,
          "Failed to read/store attachment for project " + projectId);
    }
  }

  private String saveThumbnailIfImage(Long projectId, MultipartFile file) {
    if (!isImage(file.getContentType())) {
      return null;
    }
    try (var in = file.getInputStream()) {
      var thumbnailKey = buildThumbnailStorageKey(projectId, file.getOriginalFilename());
      return tmsAttachmentDataStoreService.saveThumbnail(thumbnailKey, in);
    } catch (Exception e) {
      log.warn("Failed to create thumbnail for file {}", file.getOriginalFilename(), e);
      return null;
    }
  }

  private void deleteQuietly(String... storageKeys) {
    Arrays.stream(storageKeys)
        .filter(StringUtils::isNotBlank)
        .forEach(key -> {
          try {
            tmsAttachmentDataStoreService.delete(key);
          } catch (Exception e) {
            log.error("Failed to clean up orphaned binary {}", key, e);
          }
        });
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TmsAttachment> getTmsAttachment(Long projectId, Long attachmentId) {
    return tmsAttachmentRepository.findByIdAndProjectId(attachmentId, projectId);
  }

  @Override
  @Transactional
  public void deleteAttachment(Long projectId, Long attachmentId) {
    var attachment = tmsAttachmentRepository
        .findByIdAndProjectId(attachmentId, projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Attachment not found: " + attachmentId));

    // DB row first: an attachment still referenced fails here and keeps its binary
    try {
      tmsAttachmentRepository.delete(attachment);
      tmsAttachmentRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Attachment " + attachmentId + " is in use and cannot be deleted");
    }

    // Binaries are removed only once the deletion is committed
    var paths = Stream.of(attachment.getPathToFile(), attachment.getThumbnailPath())
        .filter(Objects::nonNull)
        .toList();
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          deleteBinaries(attachmentId, paths);
        }
      });
    } else {
      deleteBinaries(attachmentId, paths);
    }
  }

  @Override
  @Transactional
  public void removeTtlFromTmsAttachments(List<Long> attachmentIds) {
    if (CollectionUtils.isNotEmpty(attachmentIds)) {
      tmsAttachmentRepository.removeExpirationFromAttachments(attachmentIds);
    }
  }

  /**
   * Deletes expired attachments one by one, each in its own transaction, so that one attachment
   * that cannot be deleted (e.g. linked meanwhile) does not stop the cleanup of the others.
   * Binaries are deleted only after the row deletion is committed.
   */
  @Override
  public void cleanupExpiredAttachments() {
    var now = Instant.now();
    var expiredAttachments = tmsAttachmentRepository.findExpiredAttachments(now);

    if (expiredAttachments.isEmpty()) {
      return;
    }
    log.debug("Found {} expired attachments for cleanup", expiredAttachments.size());

    var transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    var deletedCount = 0;
    for (var attachment : expiredAttachments) {
      Integer deleted;
      try {
        deleted = transactionTemplate.execute(
            status -> tmsAttachmentRepository.deleteIfExpired(attachment.getId(), now));
      } catch (DataAccessException e) {
        log.warn("Failed to delete expired attachment {}: {}", attachment.getId(),
            e.getMessage());
        continue;
      }
      if (deleted != null && deleted > 0) {
        deletedCount++;
        deleteBinaries(attachment.getId(),
            Stream.of(attachment.getPathToFile(), attachment.getThumbnailPath())
                .filter(Objects::nonNull)
                .toList());
      }
    }

    log.debug("Cleaned up {} expired attachments", deletedCount);
  }

  private void deleteBinaries(Long attachmentId, List<String> paths) {
    for (var path : paths) {
      try {
        tmsAttachmentDataStoreService.delete(path);
      } catch (Exception e) {
        log.warn("Failed to delete file {} of attachment {}: {}", path, attachmentId,
            e.getMessage());
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsAttachment> getTmsAttachmentsByIds(Long projectId, List<Long> attachmentIds) {
    if (CollectionUtils.isEmpty(attachmentIds)) {
      return List.of();
    }

    var uniqueIds = Set.copyOf(attachmentIds);
    var attachments = tmsAttachmentRepository.findAllById(uniqueIds);

    validateAllFound(uniqueIds, attachments);
    validateProjectOwnership(projectId, attachments);

    return attachments;
  }

  private void validateAllFound(Set<Long> requestedIds, List<TmsAttachment> attachments) {
    var foundIds = attachments.stream()
        .map(TmsAttachment::getId)
        .collect(Collectors.toUnmodifiableSet());

    var missingIds = Sets.difference(requestedIds, foundIds);
    if (!missingIds.isEmpty()) {
      throw new ReportPortalException(ErrorType.NOT_FOUND, "Attachments not found: " + missingIds);
    }
  }

  private void validateProjectOwnership(Long projectId, List<TmsAttachment> attachments) {
    var foreignProjectIds = attachments.stream()
        .filter(attachment -> !Objects.equals(attachment.getProjectId(), projectId))
        .map(TmsAttachment::getId)
        .toList();

    if (!foreignProjectIds.isEmpty()) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "Attachments do not belong to project " + projectId + ": " + foreignProjectIds);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsAttachment> findAvailableAttachments(Long projectId, List<Long> attachmentIds) {
    if (CollectionUtils.isEmpty(attachmentIds)) {
      return List.of();
    }

    return tmsAttachmentRepository.findAllByIdInAndProjectId(attachmentIds, projectId);
  }

  @Override
  @Transactional
  public TmsAttachment duplicateTmsAttachment(TmsAttachment originalAttachment) {
    log.debug("Duplicating TMS attachment with ID: {}", originalAttachment.getId());

    try {
      // Step 1: Generate new unique filename to avoid conflicts
      var newFileName = generateDuplicateFileName(originalAttachment.getFileName());

      // Step 2: Load original file from data store and save its copy
      String newFileId;
      try (var originalFileStream = tmsAttachmentDataStoreService
          .load(originalAttachment.getPathToFile())
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
              "Original attachment file not found: " + originalAttachment.getPathToFile()))) {
        newFileId = tmsAttachmentDataStoreService.save(newFileName, originalFileStream);
      }

      // Step 3: Duplicate thumbnail if exists
      String newThumbnailId = null;
      if (originalAttachment.getThumbnailPath() != null) {
        try (var thumbnailStream = tmsAttachmentDataStoreService
            .load(originalAttachment.getThumbnailPath())
            .orElse(null)) {
          if (thumbnailStream != null) {
            String newThumbnailName = "thumbnail_" + newFileName;
            newThumbnailId = tmsAttachmentDataStoreService.save(newThumbnailName, thumbnailStream);
          }
        } catch (Exception e) {
          log.warn("Failed to duplicate thumbnail for attachment {}", originalAttachment.getId(), e);
        }
      }

      // Step 4: Create duplicated attachment entity
      var duplicatedAttachment = tmsAttachmentMapper.duplicateAttachment(originalAttachment,
          newFileId, newThumbnailId);

      // Note: TTL is not set, making duplicated attachment permanent by default
      // Note: Entity relationships (step, textManualScenario, etc.) are not copied
      // They should be set by the calling service based on context

      // Step 5: Save duplicated attachment to a database
      var savedDuplicate = tmsAttachmentRepository.save(duplicatedAttachment);

      log.debug("Successfully duplicated TMS attachment {} to new attachment {} with file: {}",
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

    // Set expires_at to configured hours from now
    var expirationTime = Instant.now().plus(ttl);

    var updatedCount = tmsAttachmentRepository.setExpirationForUnusedAttachments(expirationTime);

    log.debug("Set TTL (expires at: {}) for {} unused TMS attachments",
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

  @Override
  public void deleteAllByProjectId(Long projectId) {
    try {
      if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
        var paths = tmsAttachmentRepository.findAllByProjectId(projectId).stream()
            .flatMap(attachment -> Stream.of(attachment.getPathToFile(), attachment.getThumbnailPath()))
            .filter(Objects::nonNull)
            .toList();
        tmsAttachmentDataStoreService.deleteAll(paths, projectId.toString());
      } else {
        tmsAttachmentDataStoreService.deleteContainer(projectId.toString());
      }
    } catch (Exception e) {
      log.warn("Failed to delete TMS attachment storage for project {}: {}", projectId, e.getMessage());
    }
  }

  /**
   * Generates a unique filename for duplicated attachment. * Adds timestamp and UUID to avoid filename conflicts. * *
   *
   * @param originalFileName the original filename * @return new unique filename
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

  private boolean isImage(String contentType) {
    return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
  }

  private String buildStorageKey(Long projectId, String originalFilename) {
    var safeName = sanitizeOriginalFilename(originalFilename);
    return Paths.get(String.valueOf(projectId), UUID.randomUUID() + "_" + safeName).toString();
  }

  private String buildThumbnailStorageKey(Long projectId, String originalFilename) {
    var safeName = sanitizeOriginalFilename(originalFilename);
    return Paths.get(String.valueOf(projectId), "thumbnail_" + UUID.randomUUID() + "_" + safeName)
        .toString();
  }

  private String sanitizeOriginalFilename(String originalFilename) {
    var safeName = FilenameUtils.getName(StringUtils.defaultString(originalFilename));
    return StringUtils.isBlank(safeName) ? "attachment" : safeName;
  }
}
