package com.epam.ta.reportportal.store.database.entity.integration;

import com.epam.ta.reportportal.store.commons.JsonbUserType;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "integration", schema = "public")
@TypeDef(name = "jsonb", typeClass = JsonbUserType.class)
public class Integration implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@ManyToOne
	@JoinColumn(name = "type")
	private IntegrationType type;

	@Type(type = "jsonb")
	@Column(name = "params")
	private IntegrationParams params;

	@CreatedDate
	@Column(name = "creation_date", nullable = false)
	private LocalDateTime creationDate;

	public Integration(Long id, Project project, IntegrationType type, IntegrationParams params, LocalDateTime creationDate) {
		this.id = id;
		this.project = project;
		this.type = type;
		this.params = params;
		this.creationDate = creationDate;
	}

	public Integration() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public IntegrationType getType() {
		return type;
	}

	public void setType(IntegrationType type) {
		this.type = type;
	}

	public IntegrationParams getParams() {
		return params;
	}

	public void setParams(IntegrationParams params) {
		this.params = params;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}
}
