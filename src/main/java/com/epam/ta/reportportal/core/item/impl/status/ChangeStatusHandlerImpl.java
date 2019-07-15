package com.epam.ta.reportportal.core.item.impl.status;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.PASSED;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ChangeStatusHandlerImpl implements ChangeStatusHandler {

	private final TestItemRepository testItemRepository;
	private final IssueEntityRepository issueEntityRepository;
	private final MessageBus messageBus;
	private final LaunchRepository launchRepository;

	@Autowired
	public ChangeStatusHandlerImpl(TestItemRepository testItemRepository, IssueEntityRepository issueEntityRepository,
			MessageBus messageBus, LaunchRepository launchRepository) {
		this.testItemRepository = testItemRepository;
		this.issueEntityRepository = issueEntityRepository;
		this.messageBus = messageBus;
		this.launchRepository = launchRepository;
	}

	@Override
	public void changeParentStatus(Long childId, Long projectId, ReportPortalUser user) {
		testItemRepository.findParentByChildId(childId).ifPresent(parent -> {
			if (parent.isHasChildren()) {
				ofNullable(parent.getItemResults().getIssue()).map(IssueEntity::getIssueId).ifPresent(issueEntityRepository::deleteById);
			}
			if (isParentStatusUpdateRequired(parent)) {
				StatusEnum resolvedStatus = resolveStatus(parent.getItemId());
				if (parent.getItemResults().getStatus() != resolvedStatus) {
					TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
					parent.getItemResults().setStatus(resolvedStatus);
					messageBus.publishActivity(new TestItemStatusChangedEvent(before,
							TO_ACTIVITY_RESOURCE.apply(parent, projectId),
							user.getUserId(),
							user.getUsername()
					));
					changeParentStatus(parent.getItemId(), projectId, user);
				}
			}
		});
	}

	@Override
	public void changeLaunchStatus(Launch launch) {
		if (launch.getStatus() != StatusEnum.IN_PROGRESS) {
			if (!launchRepository.hasItemsInStatuses(launch.getId(), Lists.newArrayList(JStatusEnum.IN_PROGRESS))) {
				StatusEnum launchStatus = launchRepository.hasItemsWithStatusNotEqual(launch.getId(), StatusEnum.PASSED) ? FAILED : PASSED;
				launch.setStatus(launchStatus);
			}
		}
	}

	private boolean isParentStatusUpdateRequired(TestItem parent) {
		return parent.getItemResults().getStatus() != StatusEnum.IN_PROGRESS
				&& !testItemRepository.hasItemsInStatusByParent(parent.getItemId(), parent.getPath(), StatusEnum.IN_PROGRESS);
	}

	private StatusEnum resolveStatus(Long itemId) {
		return testItemRepository.hasDescendantsWithStatusNotEqual(itemId, JStatusEnum.PASSED) ? FAILED : PASSED;
	}
}
