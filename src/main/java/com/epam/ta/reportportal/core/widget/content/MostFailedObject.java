package com.epam.ta.reportportal.core.widget.content;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.List;

public class MostFailedObject implements Serializable {

	@Column(name = "unique_id")
	private String uniqueId;

	@Column(name = "name")
	private String name;

	@Column(name = "total")
	private Long total;

	@Column(name = "criteria")
	private Long criteria;

	@Column(name = "status_history")
	private List<String> status;

	@Column(name = "percentage")
	private Double percentage;

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Long getCriteria() {
		return criteria;
	}

	public void setCriteria(Long criteria) {
		this.criteria = criteria;
	}

	public List<String> getStatus() {
		return status;
	}

	public void setStatus(List<String> status) {
		this.status = status;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
}
