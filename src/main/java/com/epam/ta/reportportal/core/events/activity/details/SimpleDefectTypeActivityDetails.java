package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleDefectTypeActivityDetails extends JsonbObject {
	private Long defectTypeId;

	public SimpleDefectTypeActivityDetails(Long defectTypeId) {
		this.defectTypeId = defectTypeId;
	}

	public Long getDefectTypeId() {
		return defectTypeId;
	}

	public void setDefectTypeId(Long defectTypeId) {
		this.defectTypeId = defectTypeId;
	}
}
