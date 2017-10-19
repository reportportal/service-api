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
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;

/**
 * Factory for demo data facades. <br>
 * Since we have several statistics calculation implementations, we have to
 * decide in runtime which demo data should be generated
 *
 * @author Pavel Bortnik
 */
public interface DemoDataFacadeFactory {
	/**
	 * Returns demo data facade for specified strategy
	 *
	 * @param strategy
	 * @return
	 */
	DemoDataFacade getDemoDataFacade(StatisticsCalculationStrategy strategy);
}
