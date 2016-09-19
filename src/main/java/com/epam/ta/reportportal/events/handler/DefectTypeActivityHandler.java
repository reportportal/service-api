package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.ProjectSettingsRepository;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.DefectTypeCreatedEvent;
import com.epam.ta.reportportal.events.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.events.DefectTypeUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andrei Varabyeu
 */
@Component
public class DefectTypeActivityHandler {

	private static final String UPDATE_DEFECT = "update_defect";
	private static final String DEFECT_TYPE = "defect_type";
	private static final String DELETE_DEFECT = "delete_defect";

	private final ActivityRepository activityRepository;

	private final Provider<ActivityBuilder> activityBuilder;

	private final ProjectSettingsRepository projectSettingsRepository;

	@Autowired
	public DefectTypeActivityHandler(ProjectSettingsRepository projectSettingsRepository, ActivityRepository activityRepository,
			Provider<ActivityBuilder> activityBuilder) {
		this.projectSettingsRepository = projectSettingsRepository;
		this.activityRepository = activityRepository;
		this.activityBuilder = activityBuilder;
	}

	@EventListener
	public void onDefectTypeCreated(DefectTypeCreatedEvent event) {
		final Activity activity = activityBuilder.get().addLoggedObjectRef(event.getStatisticSubType().getLocator())
				.addProjectRef(event.getProject().toLowerCase()).addObjectType(DEFECT_TYPE).addActionType(UPDATE_DEFECT)
				.addUserRef(event.getUser()).build();
		activityRepository.save(activity);
	}

	@EventListener
	public void onDefectTypeUpdated(DefectTypeUpdatedEvent event) {
		List<Activity> activities = event.getRequest().getIds().stream()
				.map(r -> activityBuilder.get().addProjectRef(event.getProject()).addObjectType(DEFECT_TYPE).addActionType(UPDATE_DEFECT)
						.addLoggedObjectRef(r.getId()).addUserRef(event.getUpdatedBy()).build()).collect(Collectors.toList());
		activityRepository.save(activities);

	}

	@EventListener
	public void onDefectTypeDeleted(DefectTypeDeletedEvent event) {
		ProjectSettings projectSettings = projectSettingsRepository.findOne(event.getProject().toLowerCase());
		projectSettings.getSubTypes().values().stream().flatMap(Collection::stream)
				.filter(it -> it.getLocator().equalsIgnoreCase(event.getId())).findFirst().ifPresent(subType -> {
			Activity activity = activityBuilder.get().addProjectRef(event.getProject().toLowerCase()).addObjectType(DEFECT_TYPE)
					.addActionType(DELETE_DEFECT).addLoggedObjectRef(event.getId()).addUserRef(event.getUpdatedBy().toLowerCase())
					.addObjectName(subType.getLongName()).build();
			activityRepository.save(activity);
		});

	}
}
