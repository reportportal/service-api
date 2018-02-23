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

import com.epam.ta.reportportal.database.ApplicationContextAwareFactoryBean;
import org.springframework.data.repository.support.Repositories;

/**
 * Factory bean for {@link Repositories} object
 *
 * @author Andrei Varabyeu
 */
public class RepositoriesFactoryBean extends ApplicationContextAwareFactoryBean<Repositories> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return Repositories.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.ta.reportportal.util.ApplicationContextAwareFactoryBean#
	 * createInstance()
	 */
	@Override
	protected Repositories createInstance() {
		return new Repositories(getApplicationContext());
	}

}