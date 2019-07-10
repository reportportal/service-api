package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_PATTERN;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternCreatedEvent extends AbstractEvent implements ActivityEvent {

	private PatternTemplateActivityResource patternTemplateActivityResource;

	public PatternCreatedEvent() {
	}

	public PatternCreatedEvent(Long userId, String userLogin, PatternTemplateActivityResource patternTemplateActivityResource) {
		super(userId, userLogin);
		this.patternTemplateActivityResource = patternTemplateActivityResource;
	}

	public PatternTemplateActivityResource getPatternTemplateActivityResource() {
		return patternTemplateActivityResource;
	}

	public void setPatternTemplateActivityResource(PatternTemplateActivityResource patternTemplateActivityResource) {
		this.patternTemplateActivityResource = patternTemplateActivityResource;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addActivityEntityType(PATTERN)
				.addAction(CREATE_PATTERN)
				.addObjectId(patternTemplateActivityResource.getId())
				.addObjectName(patternTemplateActivityResource.getName())
				.addProjectId(patternTemplateActivityResource.getProjectId())
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.get();
	}
}
