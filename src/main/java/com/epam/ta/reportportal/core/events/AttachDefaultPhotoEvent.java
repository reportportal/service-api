package com.epam.ta.reportportal.core.events;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class AttachDefaultPhotoEvent {

	Long userId;

	public AttachDefaultPhotoEvent(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}
}
