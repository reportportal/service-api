package com.epam.ta.reportportal.store.database.entity.external;

import com.epam.ta.reportportal.store.database.entity.enums.AuthType;
import com.epam.ta.reportportal.store.database.entity.enums.ExternalSystemType;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "external_system", schema = "public")
public class ExternalSystem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "url")
	private String url;

	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "external_system_type")
	@Enumerated(value = EnumType.STRING)
	private ExternalSystemType externalSystemType;

	@Column(name = "external_system_auth")
	@Enumerated(value = EnumType.STRING)
	private AuthType externalSystemAuth;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "domain")
	private String domain;

	@Column(name = "accessKey")
	private String accessKey;

	@Column(name = "project_name")
	private String projectName;

	@OneToMany(mappedBy = "externalSystem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<PostFormField> fields;

	public ExternalSystem() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ExternalSystemType getExternalSystemType() {
		return externalSystemType;
	}

	public void setExternalSystemType(ExternalSystemType externalSystemType) {
		this.externalSystemType = externalSystemType;
	}

	public AuthType getExternalSystemAuth() {
		return externalSystemAuth;
	}

	public void setExternalSystemAuth(AuthType externalSystemAuth) {
		this.externalSystemAuth = externalSystemAuth;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public List<PostFormField> getFields() {
		return fields;
	}

	public void setFields(List<PostFormField> fields) {
		this.fields = fields;
	}
}
