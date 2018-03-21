/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
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
package com.epam.ta.reportportal.auth.store;

import com.epam.ta.reportportal.auth.store.entity.AuthConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Andrei Varabyeu
 */
//@Repository
//public interface AuthConfigRepository extends JpaRepository<AuthConfigEntity, String>, AuthConfigRepositoryCustom {
public interface AuthConfigRepository {
	//public interface AuthConfigRepository extends JpaRepository<AuthConfigEntity, String>, AuthConfigRepositoryCustom {

	//	String DEFAULT_PROFILE = "default";
	//
	//	@Query(value = "{ '_id' : 'default'}")
	//	AuthConfigEntity findDefault();

}
