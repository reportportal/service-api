/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;

import java.util.List;
import java.util.Optional;

/**
 * @author Pavel Bortnik
 */
public interface BugTrackingSystemRepository extends ReportPortalRepository<BugTrackingSystem, Integer>, BugTrackingSystemRepositoryCustom {

	Optional<BugTrackingSystem> findByUrlAndBtsProjectAndProjectId(String url, String btsProject, Long projectId);

	Optional<BugTrackingSystem> findByIdAndProjectId(Integer id, Long projectId);

	List<BugTrackingSystem> findAllByProjectId(Long projectId);
}
