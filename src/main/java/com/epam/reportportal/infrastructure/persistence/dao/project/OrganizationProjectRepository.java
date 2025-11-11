package com.epam.reportportal.infrastructure.persistence.dao.project;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationProjectRepository {

  Page<ProjectProfile> getProjectProfileListByFilter(Queryable filter, Pageable pageable);

}
