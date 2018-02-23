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

package com.epam.ta.reportportal.store.database.support;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Finds repository instance for provided domain type object. Introduced to
 * avoid injection of default spring's
 * {@link org.springframework.data.repository.support.Repositories} because in
 * this case we are able to add some custom logic (such as search by some
 * conditions or class cast)
 *
 * @author Andrei Varabyeu
 */
public interface RepositoryProvider {

	/**
	 * Finds Repository for provided domain type
	 *
	 * @param clazz - domain type repository related to
	 * @return - {@link PagingAndSortingRepository} for provided domain type
	 */
	<T> PagingAndSortingRepository<T, ?> getRepositoryFor(Class<?> clazz);

}