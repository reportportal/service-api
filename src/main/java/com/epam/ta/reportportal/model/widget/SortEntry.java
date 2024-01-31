package com.epam.ta.reportportal.model.widget;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class SortEntry implements Serializable {

	@JsonProperty(value = "asc")
	private boolean asc;

	@JsonProperty(value = "sortingColumn")
	private String sortingColumn;

	public SortEntry() {
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public String getSortingColumn() {
		return sortingColumn;
	}

	public void setSortingColumn(String sortingColumn) {
		this.sortingColumn = sortingColumn;
	}
}
