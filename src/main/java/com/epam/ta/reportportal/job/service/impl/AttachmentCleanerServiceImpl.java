package com.epam.ta.reportportal.job.service.impl;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverContent;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class AttachmentCleanerServiceImpl implements AttachmentCleanerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentCleanerServiceImpl.class);

	private final Integer attachmentsPageSize;

	private final AttachmentRepository attachmentRepository;

	private final DataStoreService dataStoreService;

	@Autowired
	public AttachmentCleanerServiceImpl(@Value("${rp.environment.variable.clean.attach.size}") Integer attachmentsPageSize,
			AttachmentRepository attachmentRepository, @Qualifier("attachmentDataStoreService") DataStoreService dataStoreService) {
		this.attachmentsPageSize = attachmentsPageSize;
		this.attachmentRepository = attachmentRepository;
		this.dataStoreService = dataStoreService;
	}

	@Override
	public void removeOutdatedItemsAttachments(Collection<Long> itemIds, LocalDateTime before, AtomicLong attachmentsCount,
			AtomicLong thumbnailsCount) {
		List<Attachment> attachments = attachmentRepository.findByItemIdsAndLogTimeBefore(itemIds, before);
		removeAttachments(attachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	public void removeLaunchAttachments(Long launchId, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		final List<Attachment> attachments = attachmentRepository.findAllByLaunchIdIn(Collections.singletonList(launchId));
		removeAttachments(attachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	public void removeOutdatedLaunchesAttachments(Collection<Long> launchIds, LocalDateTime before, AtomicLong attachmentsCount,
			AtomicLong thumbnailsCount) {
		List<Attachment> launchAttachments = attachmentRepository.findByLaunchIdsAndLogTimeBefore(launchIds, before);
		removeAttachments(launchAttachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	public void removeProjectAttachments(Project project, LocalDateTime before, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		final AtomicLong notRemoved = new AtomicLong(0);
		iterateOverContent(attachmentsPageSize, pageable -> attachmentRepository.findByProjectIdsAndLogTimeBefore(project.getId(),
				before,
				pageable.getPageSize(),
				notRemoved.get()
		), attachments -> {
			final int removedCount = removeAttachments(attachments, attachmentsCount, thumbnailsCount);
			notRemoved.addAndGet(attachments.size() - removedCount);
		});
	}

	/**
	 * @param attachments {@link List} of {@link Attachment} to remove
	 * @param attachmentsCount total removed attachments counter
	 * @param thumbnailsCount total removed thumbnails counter
	 * @return removed count
	 */
	private int removeAttachments(Collection<Attachment> attachments, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		List<Long> attachmentIds = new ArrayList<>();
		attachments.forEach(it -> {
			try {
				ofNullable(it).ifPresent(attachment -> {
					ofNullable(attachment.getFileId()).ifPresent(fileId -> {
						dataStoreService.delete(fileId);
						attachmentsCount.addAndGet(1L);
					});
					ofNullable(attachment.getThumbnailId()).ifPresent(thumbnailId -> {
						dataStoreService.delete(thumbnailId);
						thumbnailsCount.addAndGet(1L);
					});
					attachmentIds.add(attachment.getId());
				});
			} catch (Exception ex) {
				LOGGER.debug("Error has occurred during the attachments removing", ex);
				//do nothing, because error that has occurred during the removing of current attachment shouldn't affect others
			}
		});
		if (CollectionUtils.isNotEmpty(attachmentIds)) {
			attachmentRepository.deleteAllByIds(attachmentIds);
		}
		return attachmentIds.size();
	}
}
