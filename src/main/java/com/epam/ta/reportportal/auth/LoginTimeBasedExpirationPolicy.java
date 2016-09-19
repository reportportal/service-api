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

package com.epam.ta.reportportal.auth;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.epam.ta.reportportal.database.Time;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.epam.ta.reportportal.database.entity.user.User;

/**
 * Last Login time based expiration policy. If user don't login during provided
 * amount of time, we need to expire his account
 * 
 * @author Andrei Varabyeu
 * 
 */
@Component
public class LoginTimeBasedExpirationPolicy implements ExpirationPolicy {

	private Time time;

	public LoginTimeBasedExpirationPolicy(Time time) {
		this.time = time;
	}

	@Autowired
	public LoginTimeBasedExpirationPolicy(@Value("${rp.auth.expire.account.after}") long expirationDays) {
		this.time = Time.days(expirationDays);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.epam.ta.reportportal.auth.ExpirationPolicy#getExpirationDate()
	 */
	@Override
	public Date getExpirationDate() {
		return DateUtils.addSeconds(Calendar.getInstance().getTime(), (int) (-1 * time.in(TimeUnit.SECONDS)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.epam.ta.reportportal.auth.ExpirationPolicy#isExpired(com.epam.ta.
	 * reportportal.database.entity.user.User)
	 */
	@Override
	public boolean isExpired(User user) {
		return user.getMetaInfo().getLastLogin().before(getExpirationDate());
	}

}