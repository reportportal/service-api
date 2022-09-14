package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.ws.model.log.LogResource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class PagedLogResource extends LogResource {

	private Map<Long, Integer> pagesLocation;

	public PagedLogResource() {
		pagesLocation = new LinkedHashMap<>();
	}

	public Map<Long, Integer> getPagesLocation() {
		return pagesLocation;
	}

	public void setPagesLocation(Map<Long, Integer> pagesLocation) {
		this.pagesLocation = pagesLocation;
	}
}
