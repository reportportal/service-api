package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.commons.querygen.Filter;

/**
 * @author Yauheni_Martynau
 */
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
