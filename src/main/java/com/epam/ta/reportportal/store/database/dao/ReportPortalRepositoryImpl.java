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
import com.epam.ta.reportportal.store.commons.querygen.QueryBuilder;
import org.jooq.DSLContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * @author Pavel Bortnik
 */
public class ReportPortalRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
		implements ReportPortalRepository<T, ID> {

	private final EntityManager entityManager;
	private final DSLContext dslContext;

	public ReportPortalRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager, DSLContext dslContext) {
		super(entityInformation, entityManager);
		this.entityManager = entityManager;
		this.dslContext = dslContext;
	}

	@Override
	@Transactional
	public void refresh(T t) {
		entityManager.refresh(t);
	}

	@Override
	public List<T> findByFilter(Filter filter) {
		return dslContext.fetch(QueryBuilder.newBuilder(filter).build()).into(getDomainClass());
	}
}
