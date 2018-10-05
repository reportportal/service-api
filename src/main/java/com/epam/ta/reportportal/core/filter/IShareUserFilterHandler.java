package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.springframework.data.domain.Pageable;

public interface IShareUserFilterHandler {

    /**
     * Share user filter to project
     *
     * @param projectName
     * @param filterId
     * @return list of all shared filters for the project
     */
    void shareFilter(String projectName, Long filterId);

    /**
     * Get all shared filters for specified project and user
     *
     * @param pageable       Page request
     * @param filter         Filter representation
     * @param projectName    Project Name
     * @param user           Report Portal User
     * @return {@link Iterable}
     */
    Iterable<UserFilterResource> getAllFilters(String projectName, Pageable pageable,
        Filter filter, ReportPortalUser user);

    /**
     * Get all shared filters  for specified project and user without own filters
     *
     * @param projectName    Project Name
     * @param pageable       Page request
     * @param filter         Filter representation
     * @param user           Report Portal User
     * @return {@link Iterable}
     */
    Iterable<UserFilterResource> getSharedFilters(String projectName, Pageable pageable,
        Filter filter, ReportPortalUser user);


}
