package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.user.User;

import java.util.Optional;

public interface UserRepository extends ReportPortalRepository<User, Long> {

	Optional<User> findByLogin(String login);
}
