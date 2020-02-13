/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.user.impl;

import com.epam.ta.reportportal.core.user.UserPasswordService;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.ws.model.user.ResetPasswordRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class UserPasswordServiceImpl implements UserPasswordService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void checkAndUpdate(User user, ChangePasswordRQ request) {
		expect(passwordEncoder.matches(request.getOldPassword(), user.getPassword()), Predicate.isEqual(true)).verify(FORBIDDEN_OPERATION,
				"Old password not match with stored."
		);
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
	}

	@Override
	public void encrypt(ResetPasswordRQ request) {
		request.setPassword(passwordEncoder.encode(request.getPassword()));
	}

	@Override
	public void encrypt(CreateUserRQFull request) {
		request.setPassword(passwordEncoder.encode(request.getPassword()));
	}
}
