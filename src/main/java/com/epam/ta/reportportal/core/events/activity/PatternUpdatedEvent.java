package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processBoolean;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_PATTERN;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternUpdatedEvent extends AroundEvent<PatternTemplateActivityResource> implements ActivityEvent {

	public PatternUpdatedEvent() {
	}

	public PatternUpdatedEvent(Long userId, String userLogin, PatternTemplateActivityResource before,
			PatternTemplateActivityResource after) {
		super(userId, userLogin, before, after);
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.addProjectId(getAfter().getProjectId())
				.addAction(UPDATE_PATTERN)
				.addActivityEntityType(PATTERN)
				.addHistoryField(processName(getBefore().getName(), getAfter().getName()))
				.addHistoryField(processBoolean(ActivityDetailsUtil.ENABLED, getBefore().isEnabled(), getAfter().isEnabled()))
				.get();
	}
}
