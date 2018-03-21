package com.epam.ta.reportportal.store.database.entity.user;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Andrei Varabyeu
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users", schema = "public")
public class User implements Serializable {

	private static final long serialVersionUID = 923392981;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "login")
	private String login;

	@Column(name = "password")
	private String password;

	@Column(name = "email")
	private String email;

	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Column(name = "type")
	private String type;

	@Column(name = "default_project_id")
	private Integer defaultProjectId;

	@Column(name = "full_name")
	private String fullName;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "project")
	@Fetch(value = FetchMode.JOIN)
	private List<ProjectUser> projects;

	public User() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public List<ProjectUser> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectUser> projects) {
		this.projects = projects;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getDefaultProjectId() {
		return this.defaultProjectId;
	}

	public void setDefaultProjectId(Integer defaultProjectId) {
		this.defaultProjectId = defaultProjectId;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

}
