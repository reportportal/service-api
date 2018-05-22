package com.epam.ta.reportportal.store.database.entity.project;

import com.epam.ta.reportportal.store.database.entity.external.ExternalSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.google.common.collect.Sets;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "project", schema = "public")
public class Project implements Serializable {

	private static final long serialVersionUID = -263516611;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "name")
	private String name;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<BugTrackingSystem> bugTrackingSystems = Sets.newHashSet();

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<ExternalSystem> externalSystems = Sets.newHashSet();

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<BugTrackingSystem> getBugTrackingSystems() {
		return bugTrackingSystems;
	}

	public void setBugTrackingSystems(Set<BugTrackingSystem> bugTrackingSystems) {
		this.bugTrackingSystems = bugTrackingSystems;
	}

	public Set<ExternalSystem> getExternalSystems() {
		return externalSystems;
	}

	public void setExternalSystems(Set<ExternalSystem> externalSystems) {
		this.externalSystems = externalSystems;
	}
}
