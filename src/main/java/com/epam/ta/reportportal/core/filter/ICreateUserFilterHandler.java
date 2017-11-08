/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;

import java.util.List;

/**
 * Create filter handler
 *
 * @author Aliaksei_Makayed
 */
public interface ICreateUserFilterHandler {

	/**
	 * Creates new filter
	 *
	 * @param userName
	 * @param createFilterRQ
	 * @param project
	 * @return EntryCreatedRS
	 * @throws ReportPortalException
	 */
	List<EntryCreatedRS> createFilter(String userName, String project, CollectionsRQ<CreateUserFilterRQ> createFilterRQ);
}