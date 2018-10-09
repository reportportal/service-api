package com.epam.ta.reportportal.core.activity.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.activity.IActivityHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ActivityConverter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.PROJECT_ID;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static java.util.Optional.ofNullable;

@Service
public class ActivityHandler implements IActivityHandler {

	private final ActivityRepository activityRepository;
	private final TestItemRepository testItemRepository;
	private final LaunchRepository launchRepository;
	private final ProjectRepository projectRepository;

	public ActivityHandler(ActivityRepository activityRepository, TestItemRepository testItemRepository, LaunchRepository launchRepository,
			ProjectRepository projectRepository) {
		this.activityRepository = activityRepository;
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public List<ActivityResource> getActivitiesHistory(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable) {
		Long projectId = projectDetails.getProjectId();
		projectRepository.findById(projectId).orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
		List<Activity> activities = activityRepository.findActivitiesByProjectId(projectId, filter, pageable);
		return activities.stream().map(ActivityConverter.TO_RESOURCE).collect(Collectors.toList());
	}

	@Override
	public ActivityResource getActivity(ReportPortalUser.ProjectDetails projectDetails, Long activityId) {
		ofNullable(activityId).orElseThrow(() -> new ReportPortalException(ErrorType.ACTIVITY_NOT_FOUND, activityId));
		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.ACTIVITY_NOT_FOUND, activityId));
		expect(projectDetails.getProjectId(), Predicate.isEqual(activity.getProjectId())).verify(ErrorType.TEST_ITEM_NOT_FOUND, activityId);
		return ActivityConverter.TO_RESOURCE.apply(activity);
	}

	@Override
	public List<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Long itemId, Filter filter,
			Pageable pageable) {
		TestItem testItem = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		Launch launch = launchRepository.findById(testItem.getLaunch().getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		expect(projectDetails.getProjectId(), Predicate.isEqual(launch.getProjectId())).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		List<Activity> activities = activityRepository.findActivitiesByTestItemId(itemId, filter, pageable);
		return activities.stream().map(ActivityConverter.TO_RESOURCE).collect(Collectors.toList());
	}

	@Override
	public Page<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable) {
		Long projectId = projectDetails.getProjectId();
		projectRepository.findById(projectId).orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
		filter.withCondition(new FilterCondition(Condition.EQUALS, false, projectId.toString(), PROJECT_ID));
		org.springframework.data.domain.Page<Activity> activityPage = activityRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE).apply(activityPage);
	}
}

