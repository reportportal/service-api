package com.epam.ta.reportportal.store.database.entity.user;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Andrei Varabyeu
 */
@Embeddable
public class ProjectUserId implements Serializable {

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "project_id")
	private Long projectId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectUserId that = (ProjectUserId) o;
		return Objects.equals(userId, that.userId) && Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {

		return Objects.hash(userId, projectId);
	}
}
