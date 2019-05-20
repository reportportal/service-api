package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.DELETE_PATTERN;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternDeletedEvent extends BeforeEvent<PatternTemplateActivityResource> implements ActivityEvent {

	public PatternDeletedEvent() {
	}

	public PatternDeletedEvent(Long userId, String userLogin, PatternTemplateActivityResource before) {
		super(userId, userLogin, before);
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addObjectId(getBefore().getId())
				.addObjectName(getBefore().getName())
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.addProjectId(getBefore().getProjectId())
				.addActivityEntityType(PATTERN)
				.addAction(DELETE_PATTERN)
				.get();
	}
}
