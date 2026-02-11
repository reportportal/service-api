package com.epam.reportportal.base.infrastructure.persistence.dao.project;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationProjectRepository {

  Page<ProjectProfile> getProjectProfileListByFilter(Queryable filter, Pageable pageable);

}
