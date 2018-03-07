/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/commons-dao
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
package com.epam.ta.reportportal.auth.store.entity;

import com.epam.ta.reportportal.auth.store.entity.ldap.ActiveDirectoryConfig;
import com.epam.ta.reportportal.auth.store.entity.ldap.LdapConfig;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Andrei Varabyeu
 */
//@Entity
//@Table(name = "authConfig")
public class AuthConfigEntity {

//	@Id
//	@javax.persistence.Id
	private String id;
	private LdapConfig ldap;
	private ActiveDirectoryConfig activeDirectory;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LdapConfig getLdap() {
		return ldap;
	}

	public void setLdap(LdapConfig ldap) {
		this.ldap = ldap;
	}

	public ActiveDirectoryConfig getActiveDirectory() {
		return activeDirectory;
	}

	public void setActiveDirectory(ActiveDirectoryConfig activeDirectory) {
		this.activeDirectory = activeDirectory;
	}
}
