/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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