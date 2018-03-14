package com.epam.ta.reportportal.store.database.entity.user;

import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.project.ProjectRole;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "project_user", schema = "public")
public class ProjectUser implements Serializable {

	@EmbeddedId
	private ProjectUserId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("project_id")
	private Project project;

	@Column(name = "project_role")
	private ProjectRole role;

	public ProjectUserId getId() {
		return id;
	}

	public void setId(ProjectUserId id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ProjectRole getRole() {
		return role;
	}

	public void setRole(ProjectRole role) {
		this.role = role;
	}
}
