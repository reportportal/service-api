package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleImportActivityDetails extends JsonbObject {
	private String importFileName;

	public SimpleImportActivityDetails(String importFileName) {
		this.importFileName = importFileName;
	}

	public String getImportFileName() {
		return importFileName;
	}

	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}
}
