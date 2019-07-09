/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
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
package com.epam.ta.reportportal.auth.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Updates Last Login field in database User entity
 *
 * @author Andrei Varabyeu
 */
@Component
public class UiAuthenticationSuccessEventHandler implements ApplicationListener<UiUserSignedInEvent> {

	//    @Autowired
	//    private UserRepository userRepository;
	//	@Autowired
	//	private DSLContext dsl;

	@Override
	public void onApplicationEvent(UiUserSignedInEvent event) {
		//		dsl.update(Users.USERS).set(Users.USERS.)
		//        userRepository.updateLastLoginDate(event.getAuthentication().getName(), new Date(event.getTimestamp()));
	}
}