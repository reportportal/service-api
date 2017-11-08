package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.DefectTypeCreatedEvent;
import com.epam.ta.reportportal.events.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.events.DefectTypeUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.DELETE_DEFECT;
import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.UPDATE_DEFECT;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.DEFECT_TYPE;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Andrei Varabyeu
 */
@Component
public class DefectTypeActivityHandler {

	private final ActivityRepository activityRepository;

	@Autowired
	public DefectTypeActivityHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onDefectTypeCreated(DefectTypeCreatedEvent event) {
		final Activity activity = new ActivityBuilder().addLoggedObjectRef(event.getStatisticSubType().getLocator())
				.addProjectRef(event.getProject().toLowerCase())
				.addObjectType(DEFECT_TYPE)
				.addObjectName(event.getStatisticSubType().getLongName())
				.addActionType(UPDATE_DEFECT)
				.addUserRef(event.getUser())
				.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, event.getStatisticSubType().getLongName())))
				.get();
		activityRepository.save(activity);
	}

	@EventListener
	public void onDefectTypeUpdated(DefectTypeUpdatedEvent event) {
		List<Activity> activities = event.getRequest()
				.getIds()
				.stream()
				.map(subType -> new ActivityBuilder().addProjectRef(event.getProject())
						.addObjectType(DEFECT_TYPE)
						.addObjectName(subType.getLongName())
						.addActionType(UPDATE_DEFECT)
						.addLoggedObjectRef(subType.getId())
						.addUserRef(event.getUpdatedBy())
						.get())
				.collect(Collectors.toList());
		activityRepository.save(activities);

	}

	@EventListener
	public void onDefectTypeDeleted(DefectTypeDeletedEvent event) {
		Project projectSettings = event.getBefore();
		projectSettings.getConfiguration()
				.getSubTypes()
				.values()
				.stream()
				.flatMap(Collection::stream)
				.filter(it -> it.getLocator().equalsIgnoreCase(event.getId()))
				.findFirst()
				.ifPresent(subType -> {
					Activity activity = new ActivityBuilder().addProjectRef(projectSettings.getName())
							.addObjectType(DEFECT_TYPE)
							.addActionType(DELETE_DEFECT)
							.addLoggedObjectRef(event.getId())
							.addUserRef(event.getUpdatedBy().toLowerCase())
							.addObjectName(subType.getLongName())
							.addHistory(Collections.singletonList(createHistoryField(NAME, subType.getLongName(), EMPTY_FIELD)))
							.get();
					activityRepository.save(activity);
				});
	}
}
