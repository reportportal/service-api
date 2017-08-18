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
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.events.handler.ActivityEventType.DELETE_DEFECT;
import static com.epam.ta.reportportal.events.handler.ActivityEventType.UPDATE_DEFECT;
import static com.epam.ta.reportportal.events.handler.ActivityObjectType.DEFECT_TYPE;

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
		final Activity activity = new ActivityBuilder()
                .addLoggedObjectRef(event.getStatisticSubType().getLocator())
				.addProjectRef(event.getProject().toLowerCase())
                .addObjectType(DEFECT_TYPE.getValue())
                .addObjectName(event.getStatisticSubType().getLongName())
                .addActionType(UPDATE_DEFECT.getValue())
				.addUserRef(event.getUser()).build();
		activityRepository.save(activity);
	}

	@EventListener
	public void onDefectTypeUpdated(DefectTypeUpdatedEvent event) {
		List<Activity> activities = event.getRequest().getIds()
				.stream().map(r ->
                        new ActivityBuilder()
                                .addProjectRef(event.getProject())
                                .addObjectType(DEFECT_TYPE.getValue())
                                .addActionType(UPDATE_DEFECT.getValue())
                                .addLoggedObjectRef(r.getId())
                                .addUserRef(event.getUpdatedBy())
                                .build())
				.collect(Collectors.toList());
		activityRepository.save(activities);

	}

	@EventListener
	public void onDefectTypeDeleted(DefectTypeDeletedEvent event) {
		Project projectSettings = event.getBefore();
		projectSettings.getConfiguration().getSubTypes().values().stream().flatMap(Collection::stream)
				.filter(it -> it.getLocator().equalsIgnoreCase(event.getId())).findFirst().ifPresent(subType -> {
					Activity activity = new ActivityBuilder()
                            .addProjectRef(projectSettings.getName())
                            .addObjectType(DEFECT_TYPE.getValue())
							.addActionType(DELETE_DEFECT.getValue())
                            .addLoggedObjectRef(event.getId())
                            .addUserRef(event.getUpdatedBy().toLowerCase())
							.addObjectName(subType.getLongName())
                            .build();
					activityRepository.save(activity);
				});
	}
}
