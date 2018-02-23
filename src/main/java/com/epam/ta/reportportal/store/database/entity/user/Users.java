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

package com.epam.ta.reportportal.store.database.entity.user;

import com.epam.ta.reportportal.store.database.entity.enums.UserRoleEnum;
import com.epam.ta.reportportal.store.database.entity.enums.UserTypeEnum;
import org.jooq.tools.json.JSONObject;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */

@Entity
@Table(name = "users", schema = "public", indexes = { @Index(name = "users_login_key", unique = true, columnList = "login ASC"),
		@Index(name = "users_pk", unique = true, columnList = "id ASC") })
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 32)
	private Long id;

	@Column(name = "login", unique = true, nullable = false)
	private String login;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "role", nullable = false)
	private UserRoleEnum role;

	@Column(name = "type", nullable = false)
	private UserTypeEnum type;

	@Column(name = "default_project_id", precision = 32)
	private Integer defaultProjectId;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(name = "metadata")
	private JSONObject metadata;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRoleEnum getRole() {
		return role;
	}

	public void setRole(UserRoleEnum role) {
		this.role = role;
	}

	public UserTypeEnum getType() {
		return type;
	}

	public void setType(UserTypeEnum type) {
		this.type = type;
	}

	public Integer getDefaultProjectId() {
		return defaultProjectId;
	}

	public void setDefaultProjectId(Integer defaultProjectId) {
		this.defaultProjectId = defaultProjectId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public JSONObject getMetadata() {
		return metadata;
	}

	public void setMetadata(JSONObject metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "Users{" + "id=" + id + ", login='" + login + '\'' + ", password='" + password + '\'' + ", email='" + email + '\''
				+ ", role=" + role + ", type=" + type + ", defaultProjectId=" + defaultProjectId + ", fullName='" + fullName + '\''
				+ ", metadata=" + metadata + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Users users = (Users) o;
		return Objects.equals(id, users.id) && Objects.equals(login, users.login) && Objects.equals(password, users.password)
				&& Objects.equals(email, users.email) && role == users.role && type == users.type && Objects.equals(
				defaultProjectId, users.defaultProjectId) && Objects.equals(fullName, users.fullName) && Objects.equals(
				metadata, users.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, login, password, email, role, type, defaultProjectId, fullName, metadata);
	}
}
