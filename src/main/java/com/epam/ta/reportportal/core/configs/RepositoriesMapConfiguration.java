/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.configs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.ReportPortalRepository;
import com.epam.ta.reportportal.ws.model.favorites.FavoriteResourceTypes;

/**
 * Configure map with repositories according favorites resources types
 * 
 * @author Aliaksei_Makayed
 *
 */
@Configuration
public class RepositoriesMapConfiguration {

	@Autowired
	private DashboardRepository dashboardRepository;

	@Bean(name = "favoritesRepositories")
	public Map<FavoriteResourceTypes, ReportPortalRepository<?, String>> provide() {
		Map<FavoriteResourceTypes, ReportPortalRepository<?, String>> result = new HashMap<>();
		result.put(FavoriteResourceTypes.DASHBOARD, dashboardRepository);
		return result;
	}
}