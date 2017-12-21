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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.auth.ExpirationPolicy;
import com.epam.ta.reportportal.database.dao.UserRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Expires user account if there are not logged in many time
 *
 * @author Andrei Varabyeu
 */
@Component
public class ExpireNotUsedAccountsJob implements Job {

	@Autowired
	private ExpirationPolicy expirationPolicy;

	@Autowired
	private UserRepository userRepository;

	@Override
	public void execute(JobExecutionContext context) {
		userRepository.expireUsersLoggedOlderThan(expirationPolicy.getExpirationDate());
	}

}
