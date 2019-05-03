package com.epam.ta.reportportal.core.shareable;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.ShareableEntity;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface GetShareableEntityHandler<T extends ShareableEntity> {

	/**
	 * Get {@link ShareableEntity} on which user have {@link com.epam.ta.reportportal.auth.permissions.AclReadPermission} by id
	 *
	 * @param id             {@link ShareableEntity#id}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return dashboard
	 */
	T getPermitted(Long id, ReportPortalUser.ProjectDetails projectDetails);

	/**
	 * Get {@link ShareableEntity} on which user have {@link com.epam.ta.reportportal.auth.permissions.AclFullPermission} by id
	 *
	 * @param id             {@link ShareableEntity#id}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return dashboard
	 */
	T getAdministrated(Long id, ReportPortalUser.ProjectDetails projectDetails);

}
