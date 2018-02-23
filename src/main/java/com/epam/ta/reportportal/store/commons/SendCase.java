/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.commons;

import java.util.Optional;

/**
 * Email notification cases enumerator for project settings
 *
 * @author Andrei_Ramanchuk
 */
public enum SendCase {

	//@formatter:off
	ALWAYS("always"),
	FAILED("failed"),
	TO_INVESTIGATE("to_investigate"),
	MORE_10("more_10"),
	MORE_20("more_20"),
	MORE_50("more_50");
	//@formatter:on

	private final String value;

	SendCase(String value) {
		this.value = value;
	}

	public static Optional<SendCase> findByName(String name) {
		for (SendCase send : SendCase.values()) {
			if (send.name().equals(name)) {
				return Optional.of(send);
			}
		}
		return Optional.empty();
	}

	public static boolean isPresent(String name) {
		return findByName(name).isPresent();
	}

	public String getCaseString() {
		return value;
	}
}