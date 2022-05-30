package com.epam.ta.reportportal.core.remover.user;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.entity.user.User;

import java.util.List;

public class UserContentRemover implements ContentRemover<User> {

	private final List<ContentRemover<User>> removers;

	public UserContentRemover(List<ContentRemover<User>> removers) {
		this.removers = removers;
	}

	@Override
	public void remove(User user) {
		removers.forEach(r -> r.remove(user));
	}
}
