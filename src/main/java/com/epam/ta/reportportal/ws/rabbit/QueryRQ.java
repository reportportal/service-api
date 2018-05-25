package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.store.commons.querygen.Filter;

public class QueryRQ {

	private String entity;

	private Filter filter;

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
}
