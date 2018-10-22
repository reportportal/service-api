package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.JsonMap;
import com.epam.ta.reportportal.entity.meta.MetaData;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class UserBuilder implements Supplier<User> {

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
			user.setLogin(EntityUtils.normalizeId(request.getLogin()));
			user.setPassword(HASH_FUNCTION.hashString(request.getPassword(), Charsets.UTF_8).toString());
			user.setEmail(EntityUtils.normalizeId(request.getEmail().trim()));
			user.setFullName(request.getFullName());
			user.setUserType(UserType.INTERNAL);
			user.setExpired(false);
			Map<String, Object> lastLoginData = new HashMap<>();
			lastLoginData.put("last_login", new Date());
			user.setMetadata(new JsonMap<>(lastLoginData));
		}
		return this;
	}

	public UserBuilder addUserRole(UserRole userRole) {
		user.setRole(userRole);
		return this;
	}

	public UserBuilder addDefaultProject(Project project) {
		user.setDefaultProject(project);
		return this;
	}

	@Override
	public User get() {

		//TODO check for existing of the default project etc.
		return user;
	}
}
