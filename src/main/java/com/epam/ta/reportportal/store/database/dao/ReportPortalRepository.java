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

import com.epam.ta.reportportal.store.commons.querygen.Filter;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

/**
 * @author Pavel Bortnik
 */
@NoRepositoryBean
public interface ReportPortalRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

	void refresh(T t);

	/**
	 * Executes query built for given filter and maps result set by given mapper
	 *
	 * @param filter Filter to build a query
	 * @param mapper Result set mapper
	 * @param <R>    Type of Results
	 * @return List of mapped entries found
	 */
	<R> List<R> findByFilter(Filter filter, RecordMapper<? super Record, R> mapper);

	<R> Page<R> findByFilter(Filter filter, Pageable pageable, RecordMapper<? super Record, R> mapper);

}
