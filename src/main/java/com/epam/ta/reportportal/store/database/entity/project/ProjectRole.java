package com.epam.ta.reportportal.store.database.entity.project;

import java.util.Arrays;
import java.util.Optional;

public enum ProjectRole implements Comparable<ProjectRole> {

	OPERATOR(0),
	CUSTOMER(1),
	MEMBER(2),
	PROJECT_MANAGER(3);

	private int roleLevel;

	ProjectRole(int level) {
		this.roleLevel = level;
	}

	public boolean higherThan(ProjectRole other) {
		return this.roleLevel > other.roleLevel;
	}

	public boolean lowerThan(ProjectRole other) {
		return this.roleLevel < other.roleLevel;
	}

	public boolean sameOrHigherThan(ProjectRole other) {
		return this.roleLevel >= other.roleLevel;
	}

	public static Optional<ProjectRole> forName(final String name) {
		return Arrays.stream(ProjectRole.values()).filter(role -> role.name().equalsIgnoreCase(name)).findAny();
	}
}
