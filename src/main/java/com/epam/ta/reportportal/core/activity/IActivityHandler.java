package com.epam.ta.reportportal.core.activity;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IActivityHandler {

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.entity.item.TestItem}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param filter         Filter
	 * @param pageable       Page Details
	 * @return Found activities
	 */
	List<ActivityResource> getActivitiesHistory(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);

	/**
	 * Load {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param activityId     ID of activity
	 * @return Found Activity or NOT FOUND exception
	 */
	ActivityResource getActivity(ReportPortalUser.ProjectDetails projectDetails, Long activityId);

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.entity.item.TestItem}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param itemId         ID of test item
	 * @param filter         Filter
	 * @param pageable       Page Details
	 * @return Found activities
	 */
	List<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Long itemId, Filter filter, Pageable pageable);

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param filter         Filter
	 * @param pageable       Page Details
	 * @return Found activities
	 */
	Page<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);
}
