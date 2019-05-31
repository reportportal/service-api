package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ITEM_IDS;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.LAUNCH_ID;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.PATTERN_MATCHED;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternMatchedEvent implements ActivityEvent {

	private Long launchId;

	private List<Long> itemIds;

	private PatternTemplateActivityResource patternTemplateActivityResource;

	public PatternMatchedEvent() {
	}

	public PatternMatchedEvent(Long launchId, List<Long> itemIds, PatternTemplateActivityResource patternTemplateActivityResource) {
		this.launchId = launchId;
		this.itemIds = itemIds;
		this.patternTemplateActivityResource = patternTemplateActivityResource;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public List<Long> getItemIds() {
		return itemIds;
	}

	public void setItemIds(List<Long> itemIds) {
		this.itemIds = itemIds;
	}

	public PatternTemplateActivityResource getPatternTemplateActivityResource() {
		return patternTemplateActivityResource;
	}

	public void setPatternTemplateActivityResource(PatternTemplateActivityResource patternTemplateActivityResource) {
		this.patternTemplateActivityResource = patternTemplateActivityResource;
	}

	@Override
	public Activity toActivity() {

		HistoryField itemIdField = new HistoryField();
		itemIdField.setField(ITEM_IDS);
		itemIdField.setNewValue(itemIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

		HistoryField launchIdField = new HistoryField();
		launchIdField.setField(LAUNCH_ID);
		launchIdField.setNewValue(String.valueOf(launchId));

		return new ActivityBuilder().addCreatedNow()
				.addObjectId(patternTemplateActivityResource.getId())
				.addObjectName(patternTemplateActivityResource.getName())
				.addProjectId(patternTemplateActivityResource.getProjectId())
				.addActivityEntityType(PATTERN)
				.addAction(PATTERN_MATCHED)
				.addHistoryField(Optional.of(itemIdField))
				.addHistoryField(Optional.of(launchIdField))
				.get();
	}
}
