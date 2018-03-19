package com.epam.ta.reportportal.store.database.entity.user;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

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
}
