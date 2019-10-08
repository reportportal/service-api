package com.epam.ta.reportportal.core.events.item;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemRetryEvent {

	private Long projectId;

	private Long itemId;

	public ItemRetryEvent(Long projectId, Long itemId) {
		this.projectId = projectId;
		this.itemId = itemId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getItemId() {
		return itemId;
	}
}
