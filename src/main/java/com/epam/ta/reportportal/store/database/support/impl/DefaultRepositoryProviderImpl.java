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

package com.epam.ta.reportportal.store.database.support.impl;

import com.epam.ta.reportportal.database.support.RepositoryProvider;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.support.Repositories;

/**
 * Default implementation of {@link RepositoryProvider}
 *
 * @author Andrei Varabyeu
 */
public class DefaultRepositoryProviderImpl implements RepositoryProvider {

	private Repositories repositories;

	@Autowired
	public DefaultRepositoryProviderImpl(Repositories repositories) {
		this.repositories = repositories;

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> PagingAndSortingRepository<T, ?> getRepositoryFor(Class<?> clazz) {
		return (PagingAndSortingRepository<T, ?>) repositories.getRepositoryFor(clazz)
				.orElseThrow(() -> new ReportPortalException("Repository not found"));
	}

}