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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.converter.converters.DashboardConverter;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import org.springframework.stereotype.Service;

/**
 * Resource Assembler for the {@link Dashboard} DB entity.
 *
 * @author Aliaksei_Makayed
 * @author Pavel_Bortnik
 */
@Service
public class DashboardResourceAssembler extends ResourceAssembler<Dashboard, DashboardResource> {

	@Override
	public DashboardResource toResource(Dashboard entity) {
		return DashboardConverter.TO_RESOURCE.apply(entity);
	}

}
