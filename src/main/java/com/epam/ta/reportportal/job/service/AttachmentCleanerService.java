package com.epam.ta.reportportal.job.service;

import com.epam.ta.reportportal.entity.project.Project;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface AttachmentCleanerService {

	void removeOutdatedItemsAttachments(Collection<Long> itemIds, LocalDateTime before, AtomicLong attachmentsCount,
			AtomicLong thumbnailsCount);

	void removeOutdatedLaunchesAttachments(Collection<Long> launchIds, AtomicLong attachmentsCount, AtomicLong thumbnailsCount);

	/**
	 * @param launchIds
	 * @param before
	 * @param attachmentsCount
	 * @param thumbnailsCount
	 */
	void removeOutdatedLaunchesAttachments(Collection<Long> launchIds, LocalDateTime before, AtomicLong attachmentsCount, AtomicLong thumbnailsCount);

	void removeProjectAttachments(Project project, LocalDateTime before, AtomicLong attachmentsCount, AtomicLong thumbnailsCount);
}
