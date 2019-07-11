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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class UserBuilder implements Supplier<User> {

	public static final String USER_LAST_LOGIN = "last_login";

	private static final HashFunction HASH_FUNCTION = Hashing.md5();

	private User user;

	public UserBuilder() {
		user = new User();
	}

	public UserBuilder(User user) {
		this.user = user;
	}

	public UserBuilder addCreateUserRQ(CreateUserRQConfirm request) {
		if (request != null) {
			fillUser(request.getLogin(), request.getPassword(), request.getEmail(), request.getFullName());
		}
		return this;
	}

	public UserBuilder addCreateUserFullRQ(CreateUserRQFull request) {
		ofNullable(request).ifPresent(it -> fillUser(it.getLogin(), it.getPassword(), it.getEmail(), it.getFullName()));
		return this;
	}

	public UserBuilder addUserRole(UserRole userRole) {
		user.setRole(userRole);
		return this;
	}

	@Override
	public User get() {

		//TODO check for existing of the default project etc.
		return user;
	}

	private void fillUser(String login, String password, String email, String fullName) {
		user.setLogin(EntityUtils.normalizeId(login));
		user.setPassword(HASH_FUNCTION.hashString(password, Charsets.UTF_8).toString());
		user.setEmail(EntityUtils.normalizeId(email.trim()));
		user.setFullName(fullName);
		user.setUserType(UserType.INTERNAL);
		user.setExpired(false);
		Map<String, Object> meta = new HashMap<>();
		meta.put(USER_LAST_LOGIN, new Date());
		user.setMetadata(new Metadata(meta));
	}
}
