package com.epam.ta.reportportal.job.service.impl;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverContent;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class AttachmentCleanerServiceImpl implements AttachmentCleanerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentCleanerServiceImpl.class);

	private final Integer itemPageSize;

	private final AttachmentRepository attachmentRepository;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final DataStoreService dataStoreService;

	@Autowired
	public AttachmentCleanerServiceImpl(@Value("${rp.environment.variable.clean.items.size}") Integer itemPageSize, AttachmentRepository attachmentRepository,
			LaunchRepository launchRepository, TestItemRepository testItemRepository,
			@Qualifier("attachmentDataStoreService") DataStoreService dataStoreService) {
		this.itemPageSize = itemPageSize;
		this.attachmentRepository = attachmentRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.dataStoreService = dataStoreService;
	}

	@Override
	public void removeOutdatedItemsAttachments(Collection<Long> itemIds, LocalDateTime before, AtomicLong attachmentsCount,
			AtomicLong thumbnailsCount) {
		List<Attachment> attachments = attachmentRepository.findByItemIdsAndLogTimeBefore(itemIds, before);
		removeAttachments(attachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	public void removeOutdatedLaunchesAttachments(Collection<Long> launchIds, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		List<Attachment> attachments = attachmentRepository.findAllByLaunchIdIn(launchIds);
		removeAttachments(attachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	public void removeOutdatedLaunchesAttachments(Collection<Long> launchIds, LocalDateTime before, AtomicLong attachmentsCount,
			AtomicLong thumbnailsCount) {
		List<Attachment> launchAttachments = attachmentRepository.findByLaunchIdsAndLogTimeBefore(launchIds, before);
		removeAttachments(launchAttachments, attachmentsCount, thumbnailsCount);
	}

	@Override
	@Transactional
	public void removeProjectAttachments(Project project, LocalDateTime before, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		try (Stream<Long> launchIds = launchRepository.streamIdsByStartTimeBefore(project.getId(), before)) {
			launchIds.forEach(id -> {
				iterateOverContent(itemPageSize, pageable -> testItemRepository.findTestItemIdsByLaunchId(id, pageable), ids -> {
					List<Attachment> attachments = attachmentRepository.findByItemIdsAndLogTimeBefore(ids, before);
					removeAttachments(attachments, attachmentsCount, thumbnailsCount);
				});
				removeOutdatedLaunchesAttachments(Collections.singletonList(id), before, attachmentsCount, thumbnailsCount);
			});
		} catch (Exception e) {
			//do nothing
			LOGGER.error("Error during cleaning project attachments", e);
		}
	}

	private void removeAttachments(Collection<Attachment> attachments, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
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
	}
}
