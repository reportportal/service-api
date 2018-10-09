package com.epam.ta.reportportal.core.user.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author Ivan Budaev
 */
public class UpdateUserRoleEvent extends ApplicationEvent {

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public UpdateUserRoleEvent(UpdatedRole source) {
		super(source);
	}

	public UpdatedRole getUpdatedRole() {
		return (UpdatedRole) super.getSource();
	}
}
